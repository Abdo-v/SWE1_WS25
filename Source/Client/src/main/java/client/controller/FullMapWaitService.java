package client.controller;

import client.exception.FullMapNotAvailableException;
import client.exception.GameCommunicationException;
import client.exception.MapProcessingException;

import java.time.Duration;
import java.util.Objects;

/**
 * SRP service responsible for waiting until the server provides a full map.
 */
class FullMapWaitService {

    private final GameStateSynchronizer gameStateSynchronizer;
    private final GameStateQueryService gameStateQueryService;

    public FullMapWaitService(GameStateSynchronizer gameStateSynchronizer, GameStateQueryService gameStateQueryService) {
        this.gameStateSynchronizer = Objects.requireNonNull(gameStateSynchronizer, ControllerTextConfig.REQUIRE_GAME_STATE_SYNCHRONIZER);
        this.gameStateQueryService = Objects.requireNonNull(gameStateQueryService, ControllerTextConfig.REQUIRE_GAME_STATE_QUERY_SERVICE);
    }

    /**
     * Waits until the full map is available, polling periodically.
     *
     * Throws a checked exception only when the timeout is exceeded.
     */
    public void waitForFullMap(client.model.GameState gameState, Duration timeout, Duration pollInterval)
            throws GameCommunicationException, MapProcessingException, FullMapNotAvailableException {

        Objects.requireNonNull(gameState, "gameState is required");

        Duration safeTimeout = ControllerTextConfig.defaultIfMissing(timeout, PollingDefaults.FULL_MAP_WAIT_TIMEOUT);
        Duration requestedPoll = ControllerTextConfig.defaultIfMissing(pollInterval, PollingDefaults.DEFAULT_POLL_INTERVAL);
        Duration safePoll = requestedPoll.compareTo(PollingDefaults.MIN_POLL_INTERVAL) < 0
            ? PollingDefaults.MIN_POLL_INTERVAL
            : requestedPoll;

        String gameStateId = ControllerTextConfig.safeStringOrDefault(gameState.getGameStateID(), ControllerTextConfig.UNKNOWN);

        long deadlineNanos = System.nanoTime() + safeTimeout.toNanos();
        int attempts = 0;

        while (System.nanoTime() < deadlineNanos) {
            attempts++;

            gameStateSynchronizer.synchronize(gameState);
            if (gameStateQueryService.isFullMapAvailable()) {
                return;
            }

            try {
                Thread.sleep(Math.max(0L, safePoll.toMillis()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new FullMapNotAvailableException(gameStateId, safeTimeout.toMillis(), attempts, e);
            }
        }

        throw new FullMapNotAvailableException(gameStateId, safeTimeout.toMillis(), attempts);
    }
}
