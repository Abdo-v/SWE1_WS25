package client.controller;

import client.controller.network.service.NetworkCenter;
import client.exception.FailureReason;
import client.exception.GameCommunicationException;
import client.exception.Operation;

import java.util.Objects;

/**
 * Controller-layer queries derived from server state.
 *
 * <p>Intentionally performs "best effort" null handling to match the rest of the controller layer.
 */
class GameStateQueryService {

    private final NetworkCenter networkCenter;

    public GameStateQueryService(NetworkCenter networkCenter) {
        this.networkCenter = Objects.requireNonNull(networkCenter, ControllerTextConfig.REQUIRE_NETWORK_CENTER);
    }

    /**
     * Returns whether the full map is available (node count is 100).
     *
     * <p>If the server state has no map yet, returns {@code false}.
     */
    public boolean isFullMapAvailable() throws GameCommunicationException {
        try {
            messagesbase.messagesfromserver.GameState serverGameState = pollGameState();
            try {
                return Objects.requireNonNull(serverGameState.getMap()).getMapNodes().size() == 100;
            } catch (NullPointerException e) {
                return false;
            }
        } catch (GameCommunicationException e) {
            throw e;
        } catch (Exception e) {
            throw new GameCommunicationException(
                ControllerTextConfig.ERROR_FAILED_CHECK_FULL_MAP_AVAILABILITY_PREFIX + e.getMessage(),
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
                ControllerTextConfig.ERROR_FAILED_POLL_GAME_STATE_PREFIX + e.getMessage(),
                e,
                FailureReason.UNKNOWN.code(),
                Operation.POLL_GAME_STATE,
                -1
            );
        }
    }

}

