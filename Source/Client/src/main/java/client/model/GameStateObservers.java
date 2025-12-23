package client.model;

import client.observer.util.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Manages {@link Observer} registration and notification for a {@link GameState}.
 *
 * <p>Supports classic observers (single list) as well as topic-based observers by
 * maintaining multiple observer buckets keyed by {@link GameStateEventType}.</p>
 */
final class GameStateObservers {

    private static final Logger logger = LoggerFactory.getLogger(GameStateObservers.class);

    private final GameState source;

    /** Observers that want to be notified for every change. */
    private final Set<Observer> globalObservers = new LinkedHashSet<>();

    /** Observers registered for specific event types. */
    private final Map<GameStateEventType, Set<Observer>> observersByType = new EnumMap<>(GameStateEventType.class);

    GameStateObservers(GameState source) {
        this.source = source;
    }

    void addObserver(Observer observer) {
        Optional.ofNullable(observer)
                .filter(globalObservers::add)
                .ifPresent(obs -> notifySingleObserver(obs, new GameStateEvent(source, GameStateEventType.BULK_UPDATE)));
    }

    void addObserver(GameStateEventType eventType, Observer observer) {
        Optional.ofNullable(observer).ifPresent(obs -> {
            GameStateEventType safeType = Optional.ofNullable(eventType).orElse(GameStateEventType.BULK_UPDATE);
            observersByType.computeIfAbsent(safeType, ignored -> new LinkedHashSet<>()).add(obs);
            notifySingleObserver(obs, new GameStateEvent(source, safeType));
        });
    }

    void removeObserver(Observer observer) {
        globalObservers.remove(observer);
        for (Set<Observer> bucket : observersByType.values()) {
            bucket.remove(observer);
        }
    }

    void removeObserver(GameStateEventType eventType, Observer observer) {
        Optional.ofNullable(eventType)
                .flatMap(type -> Optional.ofNullable(observersByType.get(type)))
                .ifPresent(bucket -> Optional.ofNullable(observer).ifPresent(bucket::remove));
    }

    void notifyObservers() {
        notifyObservers(new GameStateEvent(source, GameStateEventType.BULK_UPDATE));
    }

    void notifyObservers(GameStateEventType eventType) {
        notifyObservers(new GameStateEvent(source, Optional.ofNullable(eventType).orElse(GameStateEventType.BULK_UPDATE)));
    }

    void notifyObservers(GameStateEvent event) {
        notifyObserversInternal(Objects.requireNonNull(event, "event is required"));
    }

    private void notifyObserversInternal(GameStateEvent event) {
        Set<Observer> targets = new LinkedHashSet<>();
        targets.addAll(globalObservers);
        Optional.ofNullable(observersByType.get(event.type())).ifPresent(targets::addAll);

        for (Observer observer : new ArrayList<>(targets)) {
            notifySingleObserver(observer, event);
        }
    }

    private void notifySingleObserver(Observer observer, GameStateEvent event) {
        try {
            observer.update(event);
        } catch (RuntimeException ex) {
            logger.warn("Observer threw during update (type={})", event.type(), ex);
        }
    }
}
