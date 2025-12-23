package client.observer.util;

import client.model.GameStateEvent;
import client.model.GameState;

import java.util.Optional;

@FunctionalInterface
public interface Observer {
    void update(GameState gameState);

    /**
     * Receives a typed event. By default this delegates to {@link #update(GameState)}
     * to preserve backwards compatibility with classic observers.
     */
    default void update(GameStateEvent event) {
        Optional.ofNullable(event).ifPresent(e -> update(e.source()));
    }
}
