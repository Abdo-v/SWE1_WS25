package client.controller;

import client.controller.network.service.NetworkCenter;
import client.exception.FailureReason;
import client.exception.GameCommunicationException;
import client.exception.Operation;

import java.util.Objects;
import java.util.Optional;

class GameStateQueryService {

    private final NetworkCenter networkCenter;

    public GameStateQueryService(NetworkCenter networkCenter) {
        this.networkCenter = Objects.requireNonNull(networkCenter, "networkCenter is required");
    }

    public boolean isFullMapAvailable() throws GameCommunicationException {
        try {
            messagesbase.messagesfromserver.GameState serverGameState = pollGameState();
            return Optional.ofNullable(serverGameState.getMap())
                    .map(m -> m.getMapNodes().size() == 100)
                    .orElse(false);
        } catch (GameCommunicationException e) {
            throw e;
        } catch (Exception e) {
            throw new GameCommunicationException(
                "Failed to check full map availability: " + e.getMessage(),
                e,
                FailureReason.UNKNOWN.code(),
                Operation.FULL_MAP_CHECK,
                -1
            );
        }
    }

    private messagesbase.messagesfromserver.GameState pollGameState() throws GameCommunicationException {
        try {
            return networkCenter.pollGameState();
        } catch (GameCommunicationException e) {
            throw e;
        } catch (Exception e) {
            throw new GameCommunicationException(
                "Failed to poll game state from server: " + e.getMessage(),
                e,
                FailureReason.UNKNOWN.code(),
                Operation.POLL_GAME_STATE,
                -1
            );
        }
    }

}

