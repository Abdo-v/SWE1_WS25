package client.model;

import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.observer.util.Changed;
import client.observer.util.EventSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Client-side aggregate of the current game state.
 *
 * <p>This class acts as both:
 * <ul>
 *   <li>a mutable holder of the latest state snapshot (players, map, known positions), and</li>
 *   <li>an event source for observers/UI that want to react to changes.</li>
 * </ul>
 *
 * <p>{@link #updateGameState(GameState)} applies a new snapshot and publishes fine-grained
 * change events (old/new) before emitting a bulk update.
 */
public class GameState implements client.observer.util.Observable {

    private final GameStateData data;
    private final GameStateSignals signals;
    private final GameStateQueries queries = new GameStateQueries();

    public GameState(String id) {
        this(id, new ArrayList<>(), Optional.empty());
    }

    public GameState(String id, ArrayList<PlayerState> players, GameMap map) {
        this(id, players, Optional.of(Objects.requireNonNull(map, "map is required")));
    }

    private GameState(String id, ArrayList<PlayerState> players, Optional<GameMap> map) {
        data = new GameStateData(
                Objects.requireNonNull(id, "game state ID is required"),
                Objects.requireNonNull(players, "players are required"),
                Objects.requireNonNull(map, "map is required")
        );
        signals = new GameStateSignals(this);
    }

    @Override public void addObserver(client.observer.util.Observer o) { signals.addObserver(o); }
    @Override public void addObserver(GameStateEventType t, client.observer.util.Observer o) { signals.addObserver(t, o); }
    @Override public void removeObserver(client.observer.util.Observer o) { signals.removeObserver(o); }
    @Override public void removeObserver(GameStateEventType t, client.observer.util.Observer o) { signals.removeObserver(t, o); }
    @Override public void notifyObservers() { signals.publishBulkUpdate(); }
    @Override public void notifyObservers(GameStateEventType t) { signals.publishSimpleEvent(t); }

    public EventSource<Changed<Optional<GameMap>>> mapChanges() { return signals.mapChanges(); }
    public EventSource<Changed<List<PlayerState>>> playerListChanges() { return signals.playerListChanges(); }
    public EventSource<Changed<Boolean>> treasureCollectedChanges() { return signals.treasureCollectedChanges(); }
    public EventSource<Changed<Boolean>> opponentFortFoundChanges() { return signals.opponentFortFoundChanges(); }
    public EventSource<Changed<Optional<MapNode>>> treasurePositionChanges() { return signals.treasurePositionChanges(); }
    public EventSource<Changed<Optional<MapNode>>> opponentFortPositionChanges() { return signals.opponentFortPositionChanges(); }

    /**
     * Replaces the current snapshot with {@code gameState} and publishes change events.
     *
     * <p>The snapshot is treated as authoritative: all tracked fields are overwritten.
     */
    public void updateGameState(GameState gameState) {
        GameState requiredState = Objects.requireNonNull(gameState, "game state is required");

        List<PlayerState> oldPlayers = List.copyOf(data.players());
        Optional<GameMap> oldMap = data.map();
        boolean oldTreasureCollected = data.treasureCollected();
        boolean oldOpponentFortFound = data.opponentFortFound();
        Optional<MapNode> oldTreasurePosition = data.treasurePosition();
        Optional<MapNode> oldOpponentFortPosition = data.opponentFortPosition();

        data.players(requiredState.getPlayers());
        data.map(requiredState.getMap());
        data.treasureCollected(requiredState.isTreasureCollected());
        data.opponentFortFound(requiredState.isOpponentFortFound());
        data.treasurePosition(requiredState.getTreasurePosition());
        data.opponentFortPosition(requiredState.getOpponentFortPosition());

        signals.publishPlayerListChanged(oldPlayers, List.copyOf(data.players()));
        signals.publishMapChanged(oldMap, data.map());
        signals.publishTreasureCollectedChanged(oldTreasureCollected, data.treasureCollected());
        signals.publishOpponentFortFoundChanged(oldOpponentFortFound, data.opponentFortFound());
        signals.publishTreasurePositionChanged(oldTreasurePosition, data.treasurePosition());
        signals.publishOpponentFortPositionChanged(oldOpponentFortPosition, data.opponentFortPosition());
        signals.publishBulkUpdate();
    }

    void discoverOpponentFortAt(MapNode node) {
        data.opponentFortFound(true);
        data.opponentFortPosition(Optional.of(node));
    }

    public Optional<PlayerState> getCurrentPlayerState() {
        return data.players().isEmpty() ? Optional.empty() : Optional.of(data.players().get(0));
    }

    public String getGameStateID() { return data.gameStateID(); }

    public Optional<MapNode> getOpponentFortPosition() { return data.opponentFortPosition(); }

    public void setOpponentFortPosition(MapNode opponentFortPosition) {
        Optional<MapNode> oldPosition = data.opponentFortPosition();
        Optional<MapNode> newPosition = Optional.of(Objects.requireNonNull(opponentFortPosition, "opponent fort position is required"));
        data.opponentFortPosition(newPosition);
        data.opponentFortFound(true);
        signals.publishOpponentFortPositionChanged(oldPosition, newPosition);
        signals.publishTypedEvent(GameStateEventType.OPPONENT_FORT_POSITION_CHANGED, oldPosition.map(pos -> (Object) pos), newPosition.map(pos -> (Object) pos));
    }

    public ArrayList<PlayerState> getPlayers() { return data.players(); }
    public void addPlayer(PlayerState player) { data.players().add(Objects.requireNonNull(player, "player is required")); }
    public Optional<GameMap> getMap() { return data.map(); }

    public boolean isTreasureCollected() { return data.treasureCollected(); }

    public void setTreasureCollected(boolean treasureCollected) {
        boolean oldValue = data.treasureCollected();
        data.treasureCollected(treasureCollected);
        signals.publishTreasureCollectedChanged(oldValue, data.treasureCollected());
        signals.publishTypedEvent(GameStateEventType.TREASURE_COLLECTED_CHANGED, Optional.of(oldValue), Optional.of(data.treasureCollected()));
    }

    public boolean isOpponentFortFound() { return data.opponentFortFound(); }

    public void setOpponentFortFound(boolean opponentFortFound) {
        boolean oldValue = data.opponentFortFound();
        data.opponentFortFound(opponentFortFound);
        signals.publishOpponentFortFoundChanged(oldValue, data.opponentFortFound());
        signals.publishTypedEvent(GameStateEventType.OPPONENT_FORT_FOUND_CHANGED, Optional.of(oldValue), Optional.of(data.opponentFortFound()));
    }

    public Optional<MapNode> getTreasurePosition() { return data.treasurePosition(); }

    public void setTreasurePosition(MapNode treasurePosition) {
        Optional<MapNode> oldPosition = data.treasurePosition();
        Optional<MapNode> newPosition = Optional.of(Objects.requireNonNull(treasurePosition, "treasure position is required"));
        data.treasurePosition(newPosition);
        signals.publishTreasurePositionChanged(oldPosition, newPosition);
        signals.publishTypedEvent(GameStateEventType.TREASURE_POSITION_CHANGED, oldPosition.map(pos -> (Object) pos), newPosition.map(pos -> (Object) pos));
    }

    public Optional<MapNode> getOwnFortPosition() { return data.map().flatMap(queries::getOwnFortPosition); }
    public Optional<MapNode> getEnemyCurrentPosition() { return queries.getEnemyCurrentPosition(this); }
    public Optional<PlayerState> getEnemyPlayerState() { return queries.getEnemyPlayerState(this); }

    public boolean isPlayerInOwnHalfMap() {
        return getCurrentPlayerState()
                .flatMap(PlayerState::getCurrentPosition)
                .flatMap(node -> data.map().map(gameMap -> queries.isPlayerInOwnHalfMap(gameMap, node)))
                .orElse(false);
    }

    @Override
    public String toString() {
        return "GameState{" +
                "gameStateID='" + data.gameStateID() + '\'' +
                ", players=" + data.players() +
                ", map=" + data.map().map(Object::toString).orElse("<absent>") +
                ", treasureCollected=" + data.treasureCollected() +
                ", opponentFortFound=" + data.opponentFortFound() +
                ", treasurePosition=" + data.treasurePosition().map(Object::toString).orElse("<absent>") +
                ", opponentFortPosition=" + data.opponentFortPosition().map(Object::toString).orElse("<absent>") +
                '}';
    }
}
