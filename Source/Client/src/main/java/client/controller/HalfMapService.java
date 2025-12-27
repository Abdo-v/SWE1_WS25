package client.controller;

import client.controller.network.service.NetworkCenter;
import client.exception.FailureReason;
import client.exception.GameCommunicationException;
import client.exception.GameStateException;
import client.exception.Operation;
import client.model.common.Notification;
import client.model.mapper.HalfMapDimensions;
import client.model.mapper.generator.MapGenerator;
import client.model.mapper.validator.MapValidator;
import client.model.mapper.PlayerHalfMap;
import client.view.CLIHandler;
import client.view.GameOutput;
import client.view.MapGenerationView;
import client.view.MapValidationInternalsView;

import java.util.Objects;
import java.util.Optional;

class HalfMapService {

    private static final int MAX_HALF_MAP_GENERATION_ATTEMPTS = 25;

    private final NetworkCenter networkCenter;
    private final GameOutput output;
    private final MapGenerator mapGenerator;
    private final MapValidator mapValidator;
    private final MapGenerationView mapGenerationView;
    private final MapValidationInternalsView mapValidationInternalsView;

    public HalfMapService(NetworkCenter networkCenter, CLIHandler cliHandler, GameOutput output) {
        this(
                Objects.requireNonNull(networkCenter, "networkCenter is required"),
                Objects.requireNonNull(output, "output is required"),
                new MapGenerator(),
                new MapValidator(),
                new MapGenerationView(),
                new MapValidationInternalsView()
        );
    }

    HalfMapService(
            NetworkCenter networkCenter,
            GameOutput output,
            MapGenerator mapGenerator,
            MapValidator mapValidator,
            MapGenerationView mapGenerationView,
            MapValidationInternalsView mapValidationInternalsView
    ) {
        this.networkCenter = Objects.requireNonNull(networkCenter, "networkCenter is required");
        this.output = Objects.requireNonNull(output, "output is required");
        this.mapGenerator = Objects.requireNonNull(mapGenerator, "mapGenerator is required");
        this.mapValidator = Objects.requireNonNull(mapValidator, "mapValidator is required");
        this.mapGenerationView = Objects.requireNonNull(mapGenerationView, "mapGenerationView is required");
        this.mapValidationInternalsView = Objects.requireNonNull(mapValidationInternalsView, "mapValidationInternalsView is required");
    }

    /**
     * Generates, validates, and sends the player's half map to the server.
     */
    public void generateAndSendHalfMap(String playerId, String gameStateId) throws GameCommunicationException, GameStateException {
        String safeGameStateId = Objects.requireNonNull(gameStateId, "gameStateId is required").isBlank() ? "unknown" : gameStateId;
        String safePlayerId = Optional.ofNullable(playerId).filter(id -> !id.isBlank()).orElseThrow(() -> new GameStateException(
                "Cannot generate half map: player ID is missing",
                safeGameStateId,
                Operation.GENERATE_HALF_MAP,
                FailureReason.NO_PLAYER_ID
        ));

        try {
            PlayerHalfMap halfMapToSend = generateValidHalfMap(safePlayerId, safeGameStateId);
            mapGenerationView.printHalfMap(halfMapToSend, "Own Half Map");
            sendHalfMap(halfMapToSend);
        } catch (GameCommunicationException e) {
            throw e;
        } catch (Exception e) {
            throw new GameStateException(
                "Failed to generate or send half map: " + e.getMessage(),
                e,
                gameStateId,
                Operation.GENERATE_HALF_MAP,
                FailureReason.ERROR,
                FailureReason.UNKNOWN
            );
        }
    }

    private PlayerHalfMap generateHalfMap(String playerId) {
        return mapGenerator.generateMap(HalfMapDimensions.WIDTH, HalfMapDimensions.HEIGHT, playerId);
    }

    private PlayerHalfMap generateValidHalfMap(String playerId, String gameStateId) {
        for (int attempt = 1; attempt <= MAX_HALF_MAP_GENERATION_ATTEMPTS; attempt++) {
            PlayerHalfMap candidate = generateHalfMap(playerId);
            Notification validation = mapValidator.validate(candidate);
            if (!validation.hasErrors()) {
                output.showMapValidationOk();
                return candidate;
            }

            // Map validation errors are not handled via exceptions. Report internals and retry.
            mapValidationInternalsView.report(validation);
            output.showMapValidationFailed("Attempt " + attempt + "/" + MAX_HALF_MAP_GENERATION_ATTEMPTS + ": " + validation.getErrorMessages());
        }

        throw new GameStateException(
                "Unable to generate a valid half map after " + MAX_HALF_MAP_GENERATION_ATTEMPTS + " attempts",
                gameStateId,
                Operation.GENERATE_HALF_MAP,
                FailureReason.MAP_ERROR
        );
    }

    private void sendHalfMap(PlayerHalfMap halfMap) throws GameCommunicationException {
        try {
            networkCenter.sendHalfMap(halfMap);
        } catch (Exception e) {
            throw new GameCommunicationException(
                "Failed to send half map to server: " + e.getMessage(),
                e,
                FailureReason.UNKNOWN.code(),
                Operation.SEND_HALF_MAP,
                -1
            );
        }
    }
}

