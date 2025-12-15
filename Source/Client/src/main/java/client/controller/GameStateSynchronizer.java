package client.controller;

import client.controller.network.service.NetworkCenter;
import client.exception.GameCommunicationException;
import client.exception.MapProcessingException;
import client.model.GameState;

public class GameStateSynchronizer {

    private final NetworkCenter networkCenter;

    public GameStateSynchronizer(NetworkCenter networkCenter) {
        this.networkCenter = networkCenter;
    }

    /**
     * Polls the server, converts the game state to client format, and updates the provided shared state.
     */
    public void synchronize(GameState sharedGameState) throws GameCommunicationException, MapProcessingException {
        try {
            messagesbase.messagesfromserver.GameState serverState = networkCenter.pollGameState();

            if (serverState == null) {
                throw new MapProcessingException(
                    "Received null game state from server",
                    "GameState",
                    "server_response_validation"
                );
            }

            GameState polledState = networkCenter.convertServerGamestate(serverState);

            if (polledState == null) {
                throw new MapProcessingException(
                    "Failed to convert server game state to client format",
                    "GameState",
                    "state_conversion"
                );
            }

            sharedGameState.updateGameState(polledState);

        } catch (GameCommunicationException | MapProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new MapProcessingException(
                "Unexpected error during game state update: " + e.getMessage(),
                e,
                "GameState",
                "update_process",
                -1,
                -1,
                null
            );
        }
    }
}
