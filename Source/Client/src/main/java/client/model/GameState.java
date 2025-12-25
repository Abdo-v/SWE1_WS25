package client.model;

import client.observer.util.Changed;
import client.observer.util.EventSource;
import client.observer.util.EventStream;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class GameState implements client.observer.util.Observable {

    private final String gameStateID;
    private ArrayList<PlayerState> players;
    private Optional<GameMap> map;
    private boolean treasureCollected = false;
    private boolean opponentFortFound = false;
    private Optional<MapNode> treasurePosition;
    private Optional<MapNode> opponentFortPosition;

    private final GameStateObservers observerSupport;
    private final GameStateQueries queries;

    // Modern, generic, lambda-friendly event streams (composition).
    private final EventStream<GameStateEvent> events = new EventStream<>();
    private final EventStream<Changed<Optional<GameMap>>> mapChanges = new EventStream<>();
    private final EventStream<Changed<List<PlayerState>>> playerListChanges = new EventStream<>();
    private final EventStream<Changed<Boolean>> treasureCollectedChanges = new EventStream<>();
    private final EventStream<Changed<Boolean>> opponentFortFoundChanges = new EventStream<>();
    private final EventStream<Changed<Optional<MapNode>>> treasurePositionChanges = new EventStream<>();
    private final EventStream<Changed<Optional<MapNode>>> opponentFortPositionChanges = new EventStream<>();


    @Override
    public void addObserver(client.observer.util.Observer observer) {
        observerSupport.addObserver(observer);
    }

    @Override
    public void addObserver(GameStateEventType eventType, client.observer.util.Observer observer) {
        observerSupport.addObserver(eventType, observer);
    }

    @Override
    public void removeObserver(client.observer.util.Observer observer) {
        observerSupport.removeObserver(observer);
    }

    @Override
    public void removeObserver(GameStateEventType eventType, client.observer.util.Observer observer) {
        observerSupport.removeObserver(eventType, observer);
    }

    @Override
    public void notifyObservers() {
        publish(new GameStateEvent(this, GameStateEventType.BULK_UPDATE));
    }

    @Override
    public void notifyObservers(GameStateEventType eventType) {
        publish(new GameStateEvent(this, Objects.requireNonNull(eventType, "event type is required")));
    }

    public EventSource<Changed<Optional<GameMap>>> mapChanges() {
        return mapChanges;
    }

    public EventSource<Changed<List<PlayerState>>> playerListChanges() {
        return playerListChanges;
    }

    public EventSource<Changed<Boolean>> treasureCollectedChanges() {
        return treasureCollectedChanges;
    }

    public EventSource<Changed<Boolean>> opponentFortFoundChanges() {
        return opponentFortFoundChanges;
    }

    public EventSource<Changed<Optional<MapNode>>> treasurePositionChanges() {
        return treasurePositionChanges;
    }

    public EventSource<Changed<Optional<MapNode>>> opponentFortPositionChanges() {
        return opponentFortPositionChanges;
    }

    private void publish(GameStateEvent event) {
        GameStateEvent requiredEvent = Objects.requireNonNull(event, "event is required");
        events.publish(requiredEvent);
        observerSupport.notifyObservers(requiredEvent);
    }

    /**
     * Constructs a new GameState with the given ID.
     * 
     * @param ID The game state ID
     */
    public GameState(String ID) {
        this(Objects.requireNonNull(ID, "game state ID is required"), new ArrayList<>(), Optional.empty());
    }

    /**
     * Constructs a new GameState with the given ID, players, and map.
     * 
     * @param ID The game state ID
     * @param players Array of player states
     * @param map The game map
     */
    public GameState(String ID, ArrayList<PlayerState> players, GameMap map) {
        this(
                Objects.requireNonNull(ID, "game state ID is required"),
                Objects.requireNonNull(players, "players are required"),
                Optional.of(Objects.requireNonNull(map, "map is required"))
        );
    }

    private GameState(String ID, ArrayList<PlayerState> players, Optional<GameMap> map) {
        this.gameStateID = Objects.requireNonNull(ID, "game state ID is required");
        this.players = new ArrayList<>(Objects.requireNonNull(players, "players are required"));
        this.map = Objects.requireNonNull(map, "map is required");
        this.treasureCollected = false;
        this.opponentFortFound = false;
        this.treasurePosition = Optional.empty();
        this.opponentFortPosition = Optional.empty();

        this.observerSupport = new GameStateObservers(this);
        this.queries = new GameStateQueries();
    }

    
    /**
     * Updates this game state with data from another game state.
     * 
     * @param gameState The game state to update from
     */
    public void updateGameState(GameState gameState) {
        GameState requiredState = Objects.requireNonNull(gameState, "game state is required");
        List<PlayerState> oldPlayers = List.copyOf(this.players);
        Optional<GameMap> oldMap = this.map;
        boolean oldTreasureCollected = this.treasureCollected;
        boolean oldOpponentFortFound = this.opponentFortFound;
        Optional<MapNode> oldTreasurePosition = this.treasurePosition;
        Optional<MapNode> oldOpponentFortPosition = this.opponentFortPosition;

        this.players = new ArrayList<>(requiredState.getPlayers());
        this.map = requiredState.getMap();
        this.treasureCollected = requiredState.isTreasureCollected();
        this.opponentFortFound = requiredState.isOpponentFortFound();
        this.treasurePosition = requiredState.getTreasurePosition();
        this.opponentFortPosition = requiredState.getOpponentFortPosition();

        playerListChanges.publish(new Changed<>(oldPlayers, List.copyOf(this.players)));
        mapChanges.publish(new Changed<>(oldMap, this.map));
        treasureCollectedChanges.publish(new Changed<>(oldTreasureCollected, this.treasureCollected));
        opponentFortFoundChanges.publish(new Changed<>(oldOpponentFortFound, this.opponentFortFound));
        treasurePositionChanges.publish(new Changed<>(oldTreasurePosition, this.treasurePosition));
        opponentFortPositionChanges.publish(new Changed<>(oldOpponentFortPosition, this.opponentFortPosition));

        publish(new GameStateEvent(this, GameStateEventType.BULK_UPDATE));
    }

    void discoverOpponentFortAt(MapNode node) {
        this.opponentFortFound = true;
        this.opponentFortPosition = Optional.of(node);
    }

    public Optional<PlayerState> getCurrentPlayerState() {
        return players.isEmpty() ? Optional.empty() : Optional.of(players.get(0));
    }
    
    public String getGameStateID() {
        return gameStateID;
    }
    
    public Optional<MapNode> getOpponentFortPosition() {
        return opponentFortPosition;
    }
    
    public void setOpponentFortPosition(MapNode opponentFortPosition) {
        Optional<MapNode> oldPosition = this.opponentFortPosition;
        Optional<MapNode> newPosition = Optional.of(Objects.requireNonNull(opponentFortPosition, "opponent fort position is required"));

        this.opponentFortPosition = newPosition;
        this.opponentFortPosition.ifPresent(ignored -> this.opponentFortFound = true);

        opponentFortPositionChanges.publish(new Changed<>(oldPosition, newPosition));
        publish(new GameStateEvent(
            this,
            GameStateEventType.OPPONENT_FORT_POSITION_CHANGED,
            oldPosition.map(pos -> (Object) pos),
            newPosition.map(pos -> (Object) pos)
        ));
    }
    
    public ArrayList<PlayerState> getPlayers() {
        return players;
    }

    /**
     * Adds a player to the game state.
     * 
     * @param player The player to add.
     */
    public void addPlayer(PlayerState player) {
        players.add(Objects.requireNonNull(player, "player is required"));
    }
    

    public Optional<GameMap> getMap() {
        return map;
    }

    public boolean isTreasureCollected() {
        return treasureCollected;
    }
    
    public void setTreasureCollected(boolean treasureCollected) {
        boolean oldValue = this.treasureCollected;
        this.treasureCollected = treasureCollected;

        treasureCollectedChanges.publish(new Changed<>(oldValue, this.treasureCollected));
        publish(new GameStateEvent(
            this,
            GameStateEventType.TREASURE_COLLECTED_CHANGED,
            Optional.of(oldValue),
            Optional.of(this.treasureCollected)
        ));
    }

    public boolean isOpponentFortFound() {
        return opponentFortFound;
    }
    
    public void setOpponentFortFound(boolean opponentFortFound) {
        boolean oldValue = this.opponentFortFound;
        this.opponentFortFound = opponentFortFound;

        opponentFortFoundChanges.publish(new Changed<>(oldValue, this.opponentFortFound));
        publish(new GameStateEvent(
            this,
            GameStateEventType.OPPONENT_FORT_FOUND_CHANGED,
            Optional.of(oldValue),
            Optional.of(this.opponentFortFound)
        ));
    }

    public Optional<MapNode> getTreasurePosition() {
        return treasurePosition;
    }
    
    public void setTreasurePosition(MapNode treasurePosition) {
        Optional<MapNode> oldPosition = this.treasurePosition;
        Optional<MapNode> newPosition = Optional.of(Objects.requireNonNull(treasurePosition, "treasure position is required"));

        this.treasurePosition = newPosition;

        treasurePositionChanges.publish(new Changed<>(oldPosition, newPosition));
        publish(new GameStateEvent(
            this,
            GameStateEventType.TREASURE_POSITION_CHANGED,
            oldPosition.map(pos -> (Object) pos),
            newPosition.map(pos -> (Object) pos)
        ));
    }

    public Optional<MapNode> getOwnFortPosition(){
        return map.flatMap(queries::getOwnFortPosition);
    }

    public Optional<MapNode> getEnemyCurrentPosition(){
        return queries.getEnemyCurrentPosition(this);
    }

    public Optional<PlayerState> getEnemyPlayerState(){
        return queries.getEnemyPlayerState(this);
    }

    /**
     * Checks if the player is in their own half of the map.
     * uses queries.isPlayerInOwnHalfMap to determine this.
     * @return true if the player is in their own half, false otherwise.
     */
    public boolean isPlayerInOwnHalfMap(){
        return getCurrentPlayerState()
                .flatMap(PlayerState::getCurrentPosition)
                .flatMap(node -> map.map(gameMap -> queries.isPlayerInOwnHalfMap(gameMap, node)))
                .orElse(false);
    }

    /**
     * Converts the game state to a string representation.
     * 
     * @return The string representation of the game state.
     */
    @Override
    public String toString() {
        return "GameState{" +
                "gameStateID='" + gameStateID + '\'' +
                ", players=" + players +
                ", map=" + map.map(Object::toString).orElse("<absent>") +
                ", treasureCollected=" + treasureCollected +
                ", opponentFortFound=" + opponentFortFound +
                ", treasurePosition=" + treasurePosition.map(Object::toString).orElse("<absent>") +
                ", opponentFortPosition=" + opponentFortPosition.map(Object::toString).orElse("<absent>") +
                '}';
    }
}
