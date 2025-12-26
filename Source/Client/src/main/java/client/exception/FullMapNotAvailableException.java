package client.exception;

import java.util.Objects;

/**
 * Checked exception thrown when a full map is not available within a timeout.
 */
public class FullMapNotAvailableException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String gameStateId;
    private final long timeoutMillis;
    private final int attempts;

    public FullMapNotAvailableException(String gameStateId, long timeoutMillis, int attempts) {
        super(buildMessage(gameStateId, timeoutMillis, attempts));
        this.gameStateId = Objects.requireNonNullElse(gameStateId, "unknown");
        this.timeoutMillis = timeoutMillis;
        this.attempts = attempts;
    }

    public FullMapNotAvailableException(String gameStateId, long timeoutMillis, int attempts, Throwable cause) {
        super(buildMessage(gameStateId, timeoutMillis, attempts), cause);
        this.gameStateId = Objects.requireNonNullElse(gameStateId, "unknown");
        this.timeoutMillis = timeoutMillis;
        this.attempts = attempts;
    }

    public String getGameStateId() {
        return gameStateId;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public int getAttempts() {
        return attempts;
    }

    private static String buildMessage(String gameStateId, long timeoutMillis, int attempts) {
        String safeId = Objects.requireNonNullElse(gameStateId, "unknown");
        return "Full map not available within timeout" +
                " [GameStateId: " + safeId + "]" +
                " [TimeoutMs: " + timeoutMillis + "]" +
                " [Attempts: " + attempts + "]";
    }
}
