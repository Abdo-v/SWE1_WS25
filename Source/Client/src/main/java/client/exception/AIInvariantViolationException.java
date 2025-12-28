package client.exception;

import java.util.Objects;
import java.util.Optional;

/**
 * Unchecked exception for violations of AI invariants/contracts.
 *
 * Use when an internal assumption is broken (e.g. algorithm produced an impossible state),
 * which usually indicates a bug rather than a recoverable runtime condition.
 */
public class AIInvariantViolationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Optional<String> aiComponent;
    private final Optional<String> invariant;
    private final Optional<String> details;

    public AIInvariantViolationException(String message) {
        super(Objects.requireNonNull(message, TextCnofig.REQUIRE_MESSAGE_IS_REQUIRED));
        this.aiComponent = Optional.empty();
        this.invariant = Optional.empty();
        this.details = Optional.empty();
    }

    public AIInvariantViolationException(String message, Throwable cause) {
        super(Objects.requireNonNull(message, TextCnofig.REQUIRE_MESSAGE_IS_REQUIRED), cause);
        this.aiComponent = Optional.empty();
        this.invariant = Optional.empty();
        this.details = Optional.empty();
    }

    public AIInvariantViolationException(String message, String aiComponent, String invariant, String details) {
        super(Objects.requireNonNull(message, TextCnofig.REQUIRE_MESSAGE_IS_REQUIRED));
        this.aiComponent = Optional.ofNullable(aiComponent);
        this.invariant = Optional.ofNullable(invariant);
        this.details = Optional.ofNullable(details);
    }

    public Optional<String> getAiComponent() {
        return aiComponent;
    }

    public Optional<String> getInvariant() {
        return invariant;
    }

    public Optional<String> getDetails() {
        return details;
    }
}
