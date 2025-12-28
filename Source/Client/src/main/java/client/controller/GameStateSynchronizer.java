package client.controller;

import client.controller.network.service.NetworkCenter;
import client.exception.GameCommunicationException;
import client.exception.MapProcessingException;
import client.model.GameState;

import java.util.Objects;

/** Polls and applies the latest server game state to a shared {@link GameState}. */
class GameStateSynchronizer {

    private final NetworkCenter networkCenter;

    public GameStateSynchronizer(NetworkCenter networkCenter) {
        this.networkCenter = Objects.requireNonNull(networkCenter, ControllerTextConfig.REQUIRE_NETWORK_CENTER);
    }

    /** Polls the server, converts the snapshot, and applies it via {@link GameState#updateGameState(GameState)}. */
    public void synchronize(GameState sharedGameState) throws GameCommunicationException, MapProcessingException {
        try {
            GameState shared = Objects.requireNonNull(sharedGameState, ControllerTextConfig.REQUIRE_SHARED_GAME_STATE);

            messagesbase.messagesfromserver.GameState serverState;
            try {
            serverState = Objects.requireNonNull(networkCenter.pollGameState());
            } catch (NullPointerException e) {
            throw new MapProcessingException(
                ControllerTextConfig.ERROR_RECEIVED_MISSING_GAME_STATE_FROM_SERVER,
                ControllerTextConfig.TYPE_GAME_STATE,
                ControllerTextConfig.CONTEXT_SERVER_RESPONSE_VALIDATION
            );
            }

            GameState polledState;
            try {
            polledState = Objects.requireNonNull(networkCenter.convertServerGamestate(serverState));
            } catch (NullPointerException e) {
            throw new MapProcessingException(
                ControllerTextConfig.ERROR_FAILED_CONVERT_SERVER_GAME_STATE,
                ControllerTextConfig.TYPE_GAME_STATE,
                ControllerTextConfig.CONTEXT_STATE_CONVERSION
            );
            }

            shared.updateGameState(polledState);

        } catch (GameCommunicationException | MapProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new MapProcessingException(
                ControllerTextConfig.ERROR_UNEXPECTED_GAME_STATE_UPDATE_PREFIX + e.getMessage(),
                e,
                ControllerTextConfig.TYPE_GAME_STATE,
                ControllerTextConfig.CONTEXT_UPDATE_PROCESS,
                -1,
                -1,
                ""
            );
        }
    }
}

