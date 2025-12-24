package client.controller;

import client.controller.network.service.NetworkCenter;
import client.exception.GameCommunicationException;
import client.exception.MapProcessingException;
import client.model.GameState;

import java.util.Objects;
import java.util.Optional;

class GameStateSynchronizer {

    private final NetworkCenter networkCenter;

    public GameStateSynchronizer(NetworkCenter networkCenter) {
        this.networkCenter = Objects.requireNonNull(networkCenter, "networkCenter is required");
    }

    /**
     * Polls the server, converts the game state to client format, and updates the provided shared state.
     */
    public void synchronize(GameState sharedGameState) throws GameCommunicationException, MapProcessingException {
        try {
            GameState shared = Objects.requireNonNull(sharedGameState, "sharedGameState is required");

            messagesbase.messagesfromserver.GameState serverState = Optional.ofNullable(networkCenter.pollGameState())
                    .orElseThrow(() -> new MapProcessingException(
                            "Received missing game state from server",
                            "GameState",
                            "server_response_validation"
                    ));

            GameState polledState = Optional.ofNullable(networkCenter.convertServerGamestate(serverState))
                    .orElseThrow(() -> new MapProcessingException(
                            "Failed to convert server game state to client format",
                            "GameState",
                            "state_conversion"
                    ));

            shared.updateGameState(polledState);

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
                ""
            );
        }
    }
}

