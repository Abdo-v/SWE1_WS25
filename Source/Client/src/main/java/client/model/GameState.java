package client.model;

import client.model.mapper.GameMap;
import client.model.mapper.MapNode;

import java.util.ArrayList;
import java.util.List;
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
        observerSupport.notifyObservers(GameStateEventType.BULK_UPDATE);
    }

    @Override
    public void notifyObservers(GameStateEventType eventType) {
        observerSupport.notifyObservers(eventType);
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
        Optional.ofNullable(gameState)
                .ifPresent(state -> {
                    this.players = new ArrayList<>(state.getPlayers());
                    this.map = state.getMap();
                    this.treasureCollected = state.isTreasureCollected();
                    this.opponentFortFound = state.isOpponentFortFound();
                    this.treasurePosition = state.getTreasurePosition();
                    this.opponentFortPosition = state.getOpponentFortPosition();
                });
        notifyObservers(GameStateEventType.BULK_UPDATE);
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

        observerSupport.notifyObservers(new GameStateEvent(
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

        observerSupport.notifyObservers(new GameStateEvent(
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

        observerSupport.notifyObservers(new GameStateEvent(
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

        observerSupport.notifyObservers(new GameStateEvent(
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

        observerSupport.notifyObservers(new GameStateEvent(
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

        observerSupport.notifyObservers(new GameStateEvent(
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
