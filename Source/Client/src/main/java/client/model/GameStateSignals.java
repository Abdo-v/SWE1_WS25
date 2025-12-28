package client.model;

import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.observer.util.Changed;
import client.observer.util.EventSource;
import client.observer.util.EventStream;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Eventing support for {@link GameState}.
 *
 * <p>Maintains the observer registry and publishes both coarse ({@link GameStateEvent}) and
 * fine-grained ({@link Changed}) streams.
 */
final class GameStateSignals {

    private final GameState source;
    private final GameStateObservers observerSupport;

    private final EventStream<GameStateEvent> events = new EventStream<>();
    private final EventStream<Changed<Optional<GameMap>>> mapChanges = new EventStream<>();
    private final EventStream<Changed<List<PlayerState>>> playerListChanges = new EventStream<>();
    private final EventStream<Changed<Boolean>> treasureCollectedChanges = new EventStream<>();
    private final EventStream<Changed<Boolean>> opponentFortFoundChanges = new EventStream<>();
    private final EventStream<Changed<Optional<MapNode>>> treasurePositionChanges = new EventStream<>();
    private final EventStream<Changed<Optional<MapNode>>> opponentFortPositionChanges = new EventStream<>();

    GameStateSignals(GameState source) {
        this.source = Objects.requireNonNull(source, "source is required");
        this.observerSupport = new GameStateObservers(source);
    }

    void addObserver(client.observer.util.Observer observer) {
        observerSupport.addObserver(observer);
    }

    void addObserver(GameStateEventType eventType, client.observer.util.Observer observer) {
        observerSupport.addObserver(eventType, observer);
    }

    void removeObserver(client.observer.util.Observer observer) {
        observerSupport.removeObserver(observer);
    }

    void removeObserver(GameStateEventType eventType, client.observer.util.Observer observer) {
        observerSupport.removeObserver(eventType, observer);
    }

    EventSource<Changed<Optional<GameMap>>> mapChanges() {
        return mapChanges;
    }

    EventSource<Changed<List<PlayerState>>> playerListChanges() {
        return playerListChanges;
    }

    EventSource<Changed<Boolean>> treasureCollectedChanges() {
        return treasureCollectedChanges;
    }

    EventSource<Changed<Boolean>> opponentFortFoundChanges() {
        return opponentFortFoundChanges;
    }

    EventSource<Changed<Optional<MapNode>>> treasurePositionChanges() {
        return treasurePositionChanges;
    }

    EventSource<Changed<Optional<MapNode>>> opponentFortPositionChanges() {
        return opponentFortPositionChanges;
    }

    void publishBulkUpdate() {
        publish(new GameStateEvent(source, GameStateEventType.BULK_UPDATE));
    }

    void publishSimpleEvent(GameStateEventType eventType) {
        publish(new GameStateEvent(source, Objects.requireNonNull(eventType, "event type is required")));
    }

    void publishTypedEvent(GameStateEventType eventType, Optional<Object> oldValue, Optional<Object> newValue) {
        publish(new GameStateEvent(source, eventType, oldValue, newValue));
    }

    void publishPlayerListChanged(List<PlayerState> oldPlayers, List<PlayerState> newPlayers) {
        List<PlayerState> requiredOld = List.copyOf(Objects.requireNonNull(oldPlayers, "old players are required"));
        List<PlayerState> requiredNew = List.copyOf(Objects.requireNonNull(newPlayers, "new players are required"));
        playerListChanges.publish(new Changed<>(requiredOld, requiredNew));
    }

    void publishMapChanged(Optional<GameMap> oldMap, Optional<GameMap> newMap) {
        mapChanges.publish(new Changed<>(
                Objects.requireNonNull(oldMap, "old map is required"),
                Objects.requireNonNull(newMap, "new map is required")
        ));
    }

    void publishTreasureCollectedChanged(boolean oldValue, boolean newValue) {
        treasureCollectedChanges.publish(new Changed<>(oldValue, newValue));
    }

    void publishOpponentFortFoundChanged(boolean oldValue, boolean newValue) {
        opponentFortFoundChanges.publish(new Changed<>(oldValue, newValue));
    }

    void publishTreasurePositionChanged(Optional<MapNode> oldPosition, Optional<MapNode> newPosition) {
        treasurePositionChanges.publish(new Changed<>(
                Objects.requireNonNull(oldPosition, "old treasure position is required"),
                Objects.requireNonNull(newPosition, "new treasure position is required")
        ));
    }

    void publishOpponentFortPositionChanged(Optional<MapNode> oldPosition, Optional<MapNode> newPosition) {
        opponentFortPositionChanges.publish(new Changed<>(
                Objects.requireNonNull(oldPosition, "old opponent fort position is required"),
                Objects.requireNonNull(newPosition, "new opponent fort position is required")
        ));
    }

    private void publish(GameStateEvent event) {
        GameStateEvent requiredEvent = Objects.requireNonNull(event, "event is required");
        events.publish(requiredEvent);
        observerSupport.notifyObservers(requiredEvent);
    }
}
