package client.model;

import client.observer.util.Observer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Manages {@link Observer} registration and notification for a {@link GameState}.
 *
 * <p>Supports classic observers (single list) as well as topic-based observers by
 * maintaining multiple observer buckets keyed by {@link GameStateEventType}.</p>
 */
final class GameStateObservers {

    private final GameState source;

    /** Observers that want to be notified for every change. */
    private final Set<Observer> globalObservers = new LinkedHashSet<>();

    /** Observers registered for specific event types. */
    private final Map<GameStateEventType, Set<Observer>> observersByType = new EnumMap<>(GameStateEventType.class);

    GameStateObservers(GameState source) {
        this.source = Objects.requireNonNull(source, ModelTextConfig.REQUIRE_SOURCE_GAME_STATE);
    }

    void addObserver(Observer observer) {
        Observer requiredObserver = Objects.requireNonNull(observer, ModelTextConfig.REQUIRE_OBSERVER);
        if (globalObservers.add(requiredObserver)) {
            notifySingleObserver(requiredObserver, new GameStateEvent(source, GameStateEventType.BULK_UPDATE));
        }
    }

    void addObserver(GameStateEventType eventType, Observer observer) {
        Observer requiredObserver = Objects.requireNonNull(observer, ModelTextConfig.REQUIRE_OBSERVER);
        GameStateEventType requiredType = Objects.requireNonNull(eventType, ModelTextConfig.REQUIRE_EVENT_TYPE);

        observersByType.computeIfAbsent(requiredType, ignored -> new LinkedHashSet<>()).add(requiredObserver);
        notifySingleObserver(requiredObserver, new GameStateEvent(source, requiredType));
    }

    void removeObserver(Observer observer) {
        globalObservers.remove(observer);
        for (Set<Observer> bucket : observersByType.values()) {
            bucket.remove(observer);
        }
    }

    void removeObserver(GameStateEventType eventType, Observer observer) {
        Observer requiredObserver = Objects.requireNonNull(observer, ModelTextConfig.REQUIRE_OBSERVER);
        GameStateEventType requiredType = Objects.requireNonNull(eventType, ModelTextConfig.REQUIRE_EVENT_TYPE);
        try {
            observersByType.get(requiredType).remove(requiredObserver);
        } catch (NullPointerException ignored) {
            // no bucket registered
        }
    }

    void notifyObservers() {
        notifyObservers(new GameStateEvent(source, GameStateEventType.BULK_UPDATE));
    }

    void notifyObservers(GameStateEventType eventType) {
        notifyObservers(new GameStateEvent(source, Objects.requireNonNull(eventType, ModelTextConfig.REQUIRE_EVENT_TYPE)));
    }

    void notifyObservers(GameStateEvent event) {
        notifyObserversInternal(Objects.requireNonNull(event, ModelTextConfig.REQUIRE_EVENT));
    }

    private void notifyObserversInternal(GameStateEvent event) {
        Set<Observer> targets = new LinkedHashSet<>();
        targets.addAll(globalObservers);
        try {
            targets.addAll(observersByType.get(event.type()));
        } catch (NullPointerException ignored) {
            // no bucket registered
        }

        for (Observer observer : new ArrayList<>(targets)) {
            notifySingleObserver(observer, event);
        }
    }

    private void notifySingleObserver(Observer observer, GameStateEvent event) {
        try {
            observer.update(event);
        } catch (RuntimeException ex) {
        }
    }
}
