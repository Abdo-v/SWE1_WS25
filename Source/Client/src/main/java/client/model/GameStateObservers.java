package client.model;

import client.observer.util.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages {@link Observer} registration and notification for a {@link GameState}.
 *
 * <p>Kept as a dedicated component to apply SRP and keep {@link GameState} focused on
 * representing game data.</p>
 */
final class GameStateObservers {

    private final GameState source;
    private final List<Observer> observers = new ArrayList<>();

    GameStateObservers(GameState source) {
        this.source = source;
    }

    void addObserver(Observer observer) {
        Optional.ofNullable(observer)
                .filter(obs -> !observers.contains(obs))
                .ifPresent(obs -> {
                    observers.add(obs);
                    try {
                        obs.update(source);
                    } catch (Exception ignored) {
                    }
                });
    }

    void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    void notifyObservers() {
        for (Observer observer : new ArrayList<>(observers)) {
            try {
                observer.update(source);
            } catch (Exception ignored) {
            }
        }
    }
}
