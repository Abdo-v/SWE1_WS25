package client.exception;

import client.model.Direction;

import java.util.Objects;
import java.util.Optional;

/**
 * Checked exception that indicates the AI could not determine a valid next move,
 * but the caller can often recover by applying a safe fallback move.
 *
 * <p>This is intentionally a checked exception to force the controller layer to
 * acknowledge and handle the recovery strategy (fallback move, retry, abort, etc.).
 */
public class NoValidMoveAvailableException extends Exception {

    private static final long serialVersionUID = 1L;

    private final Optional<String> aiComponent;
    private final Optional<String> decisionContext;
    private final Optional<Direction> suggestedFallbackDirection;

    public NoValidMoveAvailableException(
            String message,
            String aiComponent,
            String decisionContext,
            Optional<Direction> suggestedFallbackDirection
    ) {
        super(Objects.requireNonNull(message, "message is required"));
        this.aiComponent = Optional.ofNullable(aiComponent);
        this.decisionContext = Optional.ofNullable(decisionContext);
        this.suggestedFallbackDirection = Objects.requireNonNull(suggestedFallbackDirection, "suggestedFallbackDirection is required");
    }

    public Optional<String> getAiComponent() {
        return aiComponent;
    }

    public Optional<String> getDecisionContext() {
        return decisionContext;
    }

    public Optional<Direction> getSuggestedFallbackDirection() {
        return suggestedFallbackDirection;
    }
}
