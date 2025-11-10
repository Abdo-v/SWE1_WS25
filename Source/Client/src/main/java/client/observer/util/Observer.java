package client.observer.util;

import client.model.GameState;

public interface Observer {
    void update(GameState gameState);
}
