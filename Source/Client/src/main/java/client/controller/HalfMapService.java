package client.controller;

import client.controller.network.service.NetworkCenter;
import client.exception.FailureReason;
import client.exception.GameCommunicationException;
import client.exception.GameStateException;
import client.exception.Operation;
import client.model.common.Notification;
import client.model.mapper.HalfMapDimensions;
import client.model.mapper.MapGenerator;
import client.model.mapper.MapValidator;
import client.model.mapper.PlayerHalfMap;
import client.view.CLIHandler;
import client.view.GameOutput;

public class HalfMapService {

    private final NetworkCenter networkCenter;
    private final CLIHandler cliHandler;
    private final GameOutput output;
    private final MapValidator mapValidator;

    public HalfMapService(NetworkCenter networkCenter, CLIHandler cliHandler, GameOutput output) {
        this.networkCenter = networkCenter;
        this.cliHandler = cliHandler;
        this.output = output;
        this.mapValidator = new MapValidator();
    }

    /**
     * Generates, validates, and sends the player's half map to the server.
     */
    public void generateAndSendHalfMap(String playerId, String gameStateId) throws GameCommunicationException, GameStateException {
        if (playerId == null) {
            throw new GameStateException(
                "Cannot generate half map: player ID is not set",
                gameStateId != null ? gameStateId : "unknown",
                Operation.GENERATE_HALF_MAP,
                FailureReason.NO_PLAYER_ID
            );
        }

        try {
            PlayerHalfMap halfMapToSend = generateHalfMap(playerId);
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
                null
            );
        }
    }

    private PlayerHalfMap generateHalfMap(String playerId) {
        MapGenerator generator = new MapGenerator();
        PlayerHalfMap halfMapToSend = generator.generateMap(HalfMapDimensions.WIDTH, HalfMapDimensions.HEIGHT, playerId);
        if (cliHandler != null) {
            cliHandler.printHalfMap(halfMapToSend, "own");
        }
        return halfMapToSend;
    }

    private void validateHalfMap(PlayerHalfMap halfMap) {
        Notification validation = mapValidator.validate(halfMap);
        if (validation.hasErrors()) {
            if (output != null) {
                output.showMapValidationFailed(validation.getErrorMessages());
            }
            throw new IllegalStateException("Generated map is invalid: " + validation.getErrorMessages());
        }

        if (output != null) {
            output.showMapValidationOk();
        }
    }

    private void sendHalfMap(PlayerHalfMap halfMap) throws GameCommunicationException {
        try {
            networkCenter.sendHalfMap(halfMap);
        } catch (Exception e) {
            throw new GameCommunicationException(
                "Failed to send half map to server: " + e.getMessage(),
                e,
                networkCenter != null ? FailureReason.UNKNOWN.code() : FailureReason.NO_NETWORK.code(),
                Operation.SEND_HALF_MAP,
                -1
            );
        }
    }
}
