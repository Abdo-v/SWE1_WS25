package client.controller;

import client.controller.network.service.NetworkCenter;
import client.exception.GameCommunicationException;
import client.exception.GameStateException;
import client.model.GameState;
import client.model.PlayerState;
import messagesbase.UniquePlayerIdentifier;

import java.util.Objects;

final class PlayerRegistrationService {

    private final NetworkCenter networkCenter;

    PlayerRegistrationService(NetworkCenter networkCenter) {
        this.networkCenter = networkCenter;
    }

    String registerPlayer(GameState gameState, String firstName, String lastName, String uAccount)
            throws GameCommunicationException, GameStateException {

        GameState state;
        try {
            state = Objects.requireNonNull(gameState);
        } catch (NullPointerException e) {
            throw new GameStateException(
                    ControllerTextConfig.ERROR_CANNOT_REGISTER_PLAYER_GAME_STATE_MISSING,
                    ControllerTextConfig.UNKNOWN,
                    ControllerTextConfig.OP_PLAYER_REGISTRATION,
                    ControllerTextConfig.MISSING
            );
        }

        try {
            UniquePlayerIdentifier playerIdentifier = networkCenter.registerPlayer(firstName, lastName, uAccount);
            String playerId = playerIdentifier.getUniquePlayerID();
            PlayerState playerState = new PlayerState(playerId, firstName, lastName, uAccount);
            state.addPlayer(playerState);
            return playerId;
        } catch (GameCommunicationException e) {
            throw e;
        } catch (Exception e) {
            throw new GameStateException(
                    ControllerTextConfig.ERROR_UNEXPECTED_PLAYER_REGISTRATION_PREFIX + e.getMessage(),
                    e,
                    state.getGameStateID(),
                    ControllerTextConfig.OP_PLAYER_REGISTRATION,
                    ControllerTextConfig.UNKNOWN,
                    ""
            );
        }
    }
}

