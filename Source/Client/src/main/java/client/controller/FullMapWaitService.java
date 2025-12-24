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

    private static final String UNKNOWN_GAME_STATE_ID = "unknown";

    private final GameStateSynchronizer gameStateSynchronizer;
    private final GameStateQueryService gameStateQueryService;

    public FullMapWaitService(GameStateSynchronizer gameStateSynchronizer, GameStateQueryService gameStateQueryService) {
        this.gameStateSynchronizer = Objects.requireNonNull(gameStateSynchronizer, "gameStateSynchronizer is required");
        this.gameStateQueryService = Objects.requireNonNull(gameStateQueryService, "gameStateQueryService is required");
    }

    /**
     * Waits until the full map is available, polling periodically.
     *
     * Throws a checked exception only when the timeout is exceeded.
     */
    public void waitForFullMap(client.model.GameState gameState, Duration timeout, Duration pollInterval)
            throws GameCommunicationException, MapProcessingException, FullMapNotAvailableException {

        Objects.requireNonNull(gameState, "gameState is required");

        Duration safeTimeout = Objects.requireNonNullElse(timeout, PollingDefaults.FULL_MAP_WAIT_TIMEOUT);
        Duration requestedPoll = Objects.requireNonNullElse(pollInterval, PollingDefaults.DEFAULT_POLL_INTERVAL);
        Duration safePoll = requestedPoll.compareTo(PollingDefaults.MIN_POLL_INTERVAL) < 0
            ? PollingDefaults.MIN_POLL_INTERVAL
            : requestedPoll;

        String gameStateId = Objects.requireNonNullElse(gameState.getGameStateID(), UNKNOWN_GAME_STATE_ID);

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
