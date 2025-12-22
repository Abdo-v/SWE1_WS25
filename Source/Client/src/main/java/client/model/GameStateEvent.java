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
}
