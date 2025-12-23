package client.controller;

import client.controller.network.service.NetworkCenter;
import client.exception.FailureReason;
import client.exception.GameCommunicationException;
import client.exception.GameStateException;
import client.exception.Operation;
import client.model.common.Notification;
import client.model.mapper.HalfMapDimensions;
import client.model.mapper.generator.MapGenerator;
import client.model.mapper.MapValidator;
import client.model.mapper.PlayerHalfMap;
import client.view.CLIHandler;
import client.view.GameOutput;
import client.view.MapGenerationView;
import client.view.MapValidationInternalsView;

import java.util.Objects;
import java.util.Optional;

public class HalfMapService {

    private final NetworkCenter networkCenter;
    private final CLIHandler cliHandler;
    private final GameOutput output;
    private final MapValidator mapValidator;
    private final MapGenerationView mapGenerationView;
    private final MapValidationInternalsView mapValidationInternalsView;

    public HalfMapService(NetworkCenter networkCenter, CLIHandler cliHandler, GameOutput output) {
        this.networkCenter = Objects.requireNonNull(networkCenter, "networkCenter is required");
        this.cliHandler = Objects.requireNonNull(cliHandler, "cliHandler is required");
        this.output = Objects.requireNonNull(output, "output is required");
        this.mapValidator = new MapValidator();
        this.mapGenerationView = new MapGenerationView();
        this.mapValidationInternalsView = new MapValidationInternalsView();
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
            PlayerHalfMap halfMapToSend = generateHalfMap(safePlayerId);
            validateHalfMap(halfMapToSend);
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
        MapGenerator generator = new MapGenerator();
        PlayerHalfMap halfMapToSend = generator.generateMap(HalfMapDimensions.WIDTH, HalfMapDimensions.HEIGHT, playerId);
        // Keep CLIHandler available for legacy wiring, but use the dedicated map generation view
        // for consistent emoji-based output.
        mapGenerationView.printHalfMap(halfMapToSend, "Own Half Map");
        return halfMapToSend;
    }

    private void validateHalfMap(PlayerHalfMap halfMap) {
        Notification validation = mapValidator.validate(halfMap);
        if (validation.hasErrors()) {
            // Technical internals go to System.err
            mapValidationInternalsView.report(validation);
            output.showMapValidationFailed(validation.getErrorMessages());
            throw new IllegalStateException("Generated map is invalid: " + validation.getErrorMessages());
        }

        output.showMapValidationOk();
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

