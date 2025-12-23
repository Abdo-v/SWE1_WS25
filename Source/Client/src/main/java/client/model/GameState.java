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
    private final GameStateVisionProcessor visionProcessor;
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

    /**
     * Subscribe to all emitted {@link GameStateEvent}s using a lambda.
     */
    public EventSource<GameStateEvent> events() {
        return events;
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
     * Constructs a new GameState with the given ID and players.
     * 
     * @param ID The game state ID
     * @param players Array of player states
     */
    public GameState(String ID, ArrayList<PlayerState> players) {
        this(ID, players, Optional.empty());
    }

    /**
     * Constructs a new GameState with the given ID.
     * 
     * @param ID The game state ID
     */
    public GameState(String ID) {
        this(ID, new ArrayList<>(), Optional.empty());
    }

    /**
     * Constructs a new GameState with the given ID, players, and map.
     * 
     * @param ID The game state ID
     * @param players Array of player states
     * @param map The game map
     */
    public GameState(String ID, ArrayList<PlayerState> players, GameMap map) {
        this(ID, players, Optional.ofNullable(map));
    }

    public GameState() {
        this("default", new ArrayList<>(), Optional.empty());
    }

    private GameState(String ID, ArrayList<PlayerState> players, Optional<GameMap> map) {
        this.gameStateID = ID;
        this.players = new ArrayList<>(Optional.ofNullable(players).orElseGet(ArrayList::new));
        this.map = Optional.ofNullable(map).orElseGet(Optional::empty);
        this.treasureCollected = false;
        this.opponentFortFound = false;
        this.treasurePosition = Optional.empty();
        this.opponentFortPosition = Optional.empty();

        this.observerSupport = new GameStateObservers(this);
        this.visionProcessor = new GameStateVisionProcessor();
        this.queries = new GameStateQueries();
    }

    
    /**
     * Updates this game state with data from another game state.
     * 
     * @param gameState The game state to update from
     */
    public void updateGameState(GameState gameState) {
        List<PlayerState> oldPlayers = List.copyOf(this.players);
        Optional<GameMap> oldMap = this.map;
        boolean oldTreasureCollected = this.treasureCollected;
        boolean oldOpponentFortFound = this.opponentFortFound;
        Optional<MapNode> oldTreasurePosition = this.treasurePosition;
        Optional<MapNode> oldOpponentFortPosition = this.opponentFortPosition;

        Optional.ofNullable(gameState)
                .ifPresent(state -> {
                    this.players = new ArrayList<>(state.getPlayers());
                    this.map = state.getMap();
                    this.treasureCollected = state.isTreasureCollected();
                    this.opponentFortFound = state.isOpponentFortFound();
                    this.treasurePosition = state.getTreasurePosition();
                    this.opponentFortPosition = state.getOpponentFortPosition();
                });

        playerListChanges.publish(new Changed<>(oldPlayers, List.copyOf(this.players)));
        mapChanges.publish(new Changed<>(oldMap, this.map));
        treasureCollectedChanges.publish(new Changed<>(oldTreasureCollected, this.treasureCollected));
        opponentFortFoundChanges.publish(new Changed<>(oldOpponentFortFound, this.opponentFortFound));
        treasurePositionChanges.publish(new Changed<>(oldTreasurePosition, this.treasurePosition));
        opponentFortPositionChanges.publish(new Changed<>(oldOpponentFortPosition, this.opponentFortPosition));

        publish(new GameStateEvent(this, GameStateEventType.BULK_UPDATE));
    }
    
    /**
     * Process vision for the current player position based on terrain type.
     * This method should be called whenever the player moves to a new position.
     * 
     * @param currentPosition The current position of the player
     */
    public void processVision(MapNode currentPosition) {
        Optional.ofNullable(currentPosition)
                .ifPresent(position -> visionProcessor.processVision(this, position));
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
        Optional<MapNode> newPosition = Optional.ofNullable(opponentFortPosition);

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
    
    public void setPlayers(ArrayList<PlayerState> players) {
        List<PlayerState> oldPlayers = List.copyOf(this.players);
        this.players = new ArrayList<>(Optional.ofNullable(players).orElseGet(ArrayList::new));
        List<PlayerState> newPlayers = List.copyOf(this.players);

        playerListChanges.publish(new Changed<>(oldPlayers, newPlayers));
        publish(new GameStateEvent(
            this,
            GameStateEventType.PLAYERS_CHANGED,
            Optional.of(oldPlayers),
            Optional.of(newPlayers)
        ));
    }

    /**
     * Adds a player to the game state.
     * 
     * @param player The player to add.
     */
    public void addPlayer(PlayerState player) {
        Optional.ofNullable(player).ifPresent(players::add);
    }
    

    public Optional<GameMap> getMap() {
        return map;
    }
    
    public void setMap(GameMap map) {
        Optional<GameMap> oldMap = this.map;
        Optional<GameMap> newMap = Optional.ofNullable(map);

        this.map = newMap;

        mapChanges.publish(new Changed<>(oldMap, newMap));
        publish(new GameStateEvent(
            this,
            GameStateEventType.MAP_CHANGED,
            oldMap.map(m -> (Object) m),
            newMap.map(m -> (Object) m)
        ));
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
        Optional<MapNode> newPosition = Optional.ofNullable(treasurePosition);

        this.treasurePosition = newPosition;

        treasurePositionChanges.publish(new Changed<>(oldPosition, newPosition));
        publish(new GameStateEvent(
            this,
            GameStateEventType.TREASURE_POSITION_CHANGED,
            oldPosition.map(pos -> (Object) pos),
            newPosition.map(pos -> (Object) pos)
        ));
    }
    
    /**
     * Gets the player state for a specific player ID.
     * 
     * @param playerID The ID of the player to find.
     * @return The player's state, or empty if not found.
     */
    public Optional<PlayerState> getPlayerByID(String playerID) {
        return players.stream()
                .filter(player -> player.getPlayerID().equals(playerID))
                .findFirst();
    }

    public Optional<MapNode> getOwnFortPosition(){
        return map.flatMap(queries::getOwnFortPosition);
    }

    public Optional<MapNode> getEnemyFortPosition(){
        return map.flatMap(queries::getEnemyFortPosition);
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
