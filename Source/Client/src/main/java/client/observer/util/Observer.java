package client.observer.util;

import client.model.GameStateEvent;
import client.model.GameState;

public interface Observer {
    void update(GameState gameState);

    /**
     * Receives a typed event. By default this delegates to {@link #update(GameState)}
     * to preserve backwards compatibility with classic observers.
     */
    default void update(GameStateEvent event) {
        if (event != null) {
            update(event.source());
        }
    }
}
