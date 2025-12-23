package client.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Event object passed to observers when the {@link GameState} changes.
 *
 * <p>Observers can either react based on {@link #type()} or ignore it and use
 * {@link #source()} like in the classic "push the whole state" observer pattern.</p>
 */
public record GameStateEvent(
        GameState source,
        GameStateEventType type,
        Optional<Object> oldValue,
        Optional<Object> newValue
) {

    public GameStateEvent(GameState source, GameStateEventType type) {
        this(source, type, Optional.empty(), Optional.empty());
    }

    public GameStateEvent {
        Objects.requireNonNull(source, "source is required");
        Objects.requireNonNull(type, "type is required");
        oldValue = Objects.requireNonNull(oldValue, "oldValue is required");
        newValue = Objects.requireNonNull(newValue, "newValue is required");
    }

    /**
     * Type-safe accessor for {@link #oldValue()}.
     */
    public <T> Optional<T> oldValueAs(Class<T> type) {
        Objects.requireNonNull(type, "type is required");
        return oldValue.filter(type::isInstance).map(type::cast);
    }

    /**
     * Type-safe accessor for {@link #newValue()}.
     */
    public <T> Optional<T> newValueAs(Class<T> type) {
        Objects.requireNonNull(type, "type is required");
        return newValue.filter(type::isInstance).map(type::cast);
    }
}
