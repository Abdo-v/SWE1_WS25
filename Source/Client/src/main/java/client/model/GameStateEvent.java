package client.model;

import java.util.Objects;

/**
 * Event object passed to observers when the {@link GameState} changes.
 *
 * <p>Observers can either react based on {@link #type()} or ignore it and use
 * {@link #source()} like in the classic "push the whole state" observer pattern.</p>
 */
public record GameStateEvent(GameState source, GameStateEventType type) {

    public GameStateEvent {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(type, "type must not be null");
    }
}
