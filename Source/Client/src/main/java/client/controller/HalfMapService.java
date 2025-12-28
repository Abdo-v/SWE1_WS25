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

/**
 * Generates a valid half-map and submits it to the server.
 *
 * <p>Generation is retried until validation succeeds or the retry limit is exceeded.
 */
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
                Objects.requireNonNull(networkCenter, ControllerTextConfig.REQUIRE_NETWORK_CENTER),
                Objects.requireNonNull(output, ControllerTextConfig.REQUIRE_OUTPUT),
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
        this.networkCenter = Objects.requireNonNull(networkCenter, ControllerTextConfig.REQUIRE_NETWORK_CENTER);
        this.output = Objects.requireNonNull(output, ControllerTextConfig.REQUIRE_OUTPUT);
        this.mapGenerator = Objects.requireNonNull(mapGenerator, ControllerTextConfig.REQUIRE_MAP_GENERATOR);
        this.mapValidator = Objects.requireNonNull(mapValidator, ControllerTextConfig.REQUIRE_MAP_VALIDATOR);
        this.mapGenerationView = Objects.requireNonNull(mapGenerationView, ControllerTextConfig.REQUIRE_MAP_GENERATION_VIEW);
        this.mapValidationInternalsView = Objects.requireNonNull(mapValidationInternalsView, ControllerTextConfig.REQUIRE_MAP_VALIDATION_INTERNALS_VIEW);
    }

    /** Generates, validates, and submits a half-map for the registered player. */
    public void generateAndSendHalfMap(String playerId, String gameStateId) throws GameCommunicationException, GameStateException {
        String safeGameStateId = Objects.requireNonNull(gameStateId, ControllerTextConfig.REQUIRE_GAME_STATE_ID).isBlank() ? ControllerTextConfig.UNKNOWN : gameStateId;
        String safePlayerId = ControllerTextConfig.optionalNonBlank(playerId).orElseThrow(() -> new GameStateException(
            ControllerTextConfig.ERROR_CANNOT_GENERATE_HALF_MAP_NO_PLAYER_ID,
                safeGameStateId,
                Operation.GENERATE_HALF_MAP,
                FailureReason.NO_PLAYER_ID
        ));

        try {
            PlayerHalfMap halfMapToSend = generateValidHalfMap(safePlayerId, safeGameStateId);
            mapGenerationView.printHalfMap(halfMapToSend, ControllerTextConfig.HALF_MAP_TITLE_OWN);
            sendHalfMap(halfMapToSend);
        } catch (GameCommunicationException e) {
            throw e;
        } catch (Exception e) {
            throw new GameStateException(
                ControllerTextConfig.ERROR_FAILED_GENERATE_OR_SEND_HALF_MAP_PREFIX + e.getMessage(),
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
                output.showMapValidationFailed(
                    ControllerTextConfig.MAP_VALIDATION_ATTEMPT_PREFIX + attempt
                        + ControllerTextConfig.MAP_VALIDATION_ATTEMPT_SEPARATOR + MAX_HALF_MAP_GENERATION_ATTEMPTS
                        + ControllerTextConfig.MAP_VALIDATION_ATTEMPT_SUFFIX + validation.getErrorMessages()
                );
        }

        throw new GameStateException(
                ControllerTextConfig.ERROR_UNABLE_TO_GENERATE_VALID_HALF_MAP_AFTER_PREFIX
                    + MAX_HALF_MAP_GENERATION_ATTEMPTS
                    + ControllerTextConfig.ERROR_UNABLE_TO_GENERATE_VALID_HALF_MAP_AFTER_SUFFIX,
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
                ControllerTextConfig.ERROR_FAILED_SEND_HALF_MAP_PREFIX + e.getMessage(),
                e,
                FailureReason.UNKNOWN.code(),
                Operation.SEND_HALF_MAP,
                -1
            );
        }
    }
}

