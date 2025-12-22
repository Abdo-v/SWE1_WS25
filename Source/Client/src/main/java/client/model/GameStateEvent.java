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
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(type, "type must not be null");
        oldValue = Optional.ofNullable(oldValue).orElseGet(Optional::empty);
        newValue = Optional.ofNullable(newValue).orElseGet(Optional::empty);
    }

    /**
     * Type-safe accessor for {@link #oldValue()}.
     */
    public <T> Optional<T> oldValueAs(Class<T> type) {
        Objects.requireNonNull(type, "type must not be null");
        return oldValue.filter(type::isInstance).map(type::cast);
    }

    /**
     * Type-safe accessor for {@link #newValue()}.
     */
    public <T> Optional<T> newValueAs(Class<T> type) {
        Objects.requireNonNull(type, "type must not be null");
        return newValue.filter(type::isInstance).map(type::cast);
    }
}
