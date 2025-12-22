package client.model;

import client.model.mapper.GameMap;
import client.model.mapper.MapNode;

import java.util.ArrayList;

/**
 * Class representing the current state of the game.
 */
public class GameState implements client.observer.util.Observable {

    private final String gameStateID;
    private ArrayList<PlayerState> players;
    private GameMap map;
    private boolean treasureCollected = false;
    private boolean opponentFortFound = false;
    private MapNode treasurePosition = null;
    private MapNode opponentFortPosition = null; 

    private final GameStateObservers observerSupport;
    private final GameStateVisionProcessor visionProcessor;
    private final GameStateQueries queries;


    @Override
    public void addObserver(client.observer.util.Observer observer) {
        observerSupport.addObserver(observer);
    }

    @Override
    public void removeObserver(client.observer.util.Observer observer) {
        observerSupport.removeObserver(observer);
    }

    @Override
    public void notifyObservers() {
        observerSupport.notifyObservers();
    }
    
    /**
     * Constructs a new GameState with the given ID and players.
     * 
     * @param ID The game state ID
     * @param players Array of player states
     */
    public GameState(String ID, ArrayList<PlayerState> players) {
        this(ID, players, null);
    }

    /**
     * Constructs a new GameState with the given ID.
     * 
     * @param ID The game state ID
     */
    public GameState(String ID) {
        this(ID, null, null);
    }

    /**
     * Constructs a new GameState with the given ID, players, and map.
     * 
     * @param ID The game state ID
     * @param players Array of player states
     * @param map The game map
     */
    public GameState(String ID, ArrayList<PlayerState> players, GameMap map) {
        this.gameStateID = ID;
        this.players = players;
        this.map = map;
        this.treasureCollected = false;
        this.opponentFortFound = false;

        this.observerSupport = new GameStateObservers(this);
        this.visionProcessor = new GameStateVisionProcessor();
        this.queries = new GameStateQueries();
    }

    public GameState() {
        this("default", new ArrayList<>(), null);
    }

    
    /**
     * Updates this game state with data from another game state.
     * 
     * @param gameState The game state to update from
     */
    public void updateGameState(GameState gameState) {
        if (gameState != null) {
            this.players = gameState.getPlayers();
            this.map = gameState.getMap();
            this.treasureCollected = gameState.isTreasureCollected();
            this.opponentFortFound = gameState.isOpponentFortFound();
            this.treasurePosition = gameState.getTreasurePosition();
            this.opponentFortPosition = gameState.getOpponentFortPosition();
        }
        notifyObservers();
    }
    
    /**
     * Process vision for the current player position based on terrain type.
     * This method should be called whenever the player moves to a new position.
     * 
     * @param currentPosition The current position of the player
     */
    public void processVision(MapNode currentPosition) {
        visionProcessor.processVision(this, currentPosition);
    }

    void discoverOpponentFortAt(MapNode node) {
        this.opponentFortFound = true;
        this.opponentFortPosition = node;
    }

    /**
     * Gets the current player's state.
     * 
     * @return The current player's state.
     */
    public PlayerState getCurrentPlayerState() {
        if (players != null && !players.isEmpty()) {
            return players.get(0);
        }
        return null;
    }
    
    /**
     * Gets the game state ID.
     * 
     * @return The game state ID.
     */
    public String getGameStateID() {
        return gameStateID;
    }
    
    /**
     * Gets the opponent's fort position.
     * 
     * @return The opponent's fort position.
     */
    public MapNode getOpponentFortPosition() {
        return opponentFortPosition;
    }
    
    /**
     * Sets the opponent's fort position.
     * 
     * @param opponentFortPosition The opponent's fort position.
     */
    public void setOpponentFortPosition(MapNode opponentFortPosition) {
        this.opponentFortPosition = opponentFortPosition;
        if (opponentFortPosition != null) {
            this.opponentFortFound = true;
        }
        notifyObservers();
    }
    
    /**
     * Gets the list of players.
     * 
     * @return The list of players.
     */
    public ArrayList<PlayerState> getPlayers() {
        return players;
    }
    
    /**
     * Sets the list of players.
     * 
     * @param players The list of players.
     */
    public void setPlayers(ArrayList<PlayerState> players) {
        this.players = players;
        notifyObservers();
    }

    /**
     * Adds a player to the game state.
     * 
     * @param player The player to add.
     */
    public void addPlayer(PlayerState player) {
        if (players != null) {
            this.players.add(player);
        } else {
            this.players = new ArrayList<PlayerState>();
            this.players.add(player);
        }
    }
    
    /**
     * Gets the game map.
     * 
     * @return The game map.
     */
    public GameMap getMap() {
        return map;
    }
    
    /**
     * Sets the game map.
     * 
     * @param map The game map.
     */
    public void setMap(GameMap map) {
        this.map = map;
        notifyObservers();
    }
    
    /**
     * Checks if the treasure has been collected.
     * 
     * @return true if the treasure has been collected, false otherwise.
     */
    public boolean isTreasureCollected() {
        return treasureCollected;
    }
    
    /**
     * Sets the treasure collected status.
     * 
     * @param treasureCollected The treasure collected status.
     */
    public void setTreasureCollected(boolean treasureCollected) {
        this.treasureCollected = treasureCollected;
        notifyObservers();
    }
    
    /**
     * Checks if the opponent's fort has been found.
     * 
     * @return true if the opponent's fort has been found, false otherwise.
     */
    public boolean isOpponentFortFound() {
        return opponentFortFound;
    }
    
    /**
     * Sets the opponent's fort found status.
     * 
     * @param opponentFortFound The opponent's fort found status.
     */
    public void setOpponentFortFound(boolean opponentFortFound) {
        this.opponentFortFound = opponentFortFound;
        notifyObservers();
    }
    
    /**
     * Gets the treasure position.
     * 
     * @return The treasure position.
     */
    public MapNode getTreasurePosition() {
        return treasurePosition;
    }
    
    /**
     * Sets the treasure position.
     * 
     * @param treasurePosition The treasure position.
     */
    public void setTreasurePosition(MapNode treasurePosition) {
        this.treasurePosition = treasurePosition;
        notifyObservers();
    }
    
    /**
     * Gets the player state for a specific player ID.
     * 
     * @param playerID The ID of the player to find.
     * @return The player's state, or null if not found.
     */
    public PlayerState getPlayerByID(String playerID) {
        if (players != null) {
            for (PlayerState player : players) {
                if (player.getPlayerID().equals(playerID)) {
                    return player;
                }
            }
        }
        return null;
    }

    /**
     * Gets the player's own fort position.
     * 
     * @return The player's own fort position.
     */
    public MapNode getOwnFortPosition(){
        return queries.getOwnFortPosition(map);
    }

    /**
     * Gets the opponent's fort position.
     * 
     * @return The opponent's fort position.
     */
    public MapNode getEnemyFortPosition(){
        return queries.getEnemyFortPosition(map);
    }

    public MapNode getEnemyCurrentPosition(){
        return queries.getEnemyCurrentPosition(this);
    }

    public PlayerState getEnemyPlayerState(){
        return queries.getEnemyPlayerState(this);
    }

    /**
     * Checks if the player is in their own half of the map.
     * uses getCurrentPlayerState().getCurrentPosition() and and getOrientation().
     * @return true if the player is in their own half, false otherwise.
     */

    public boolean isPlayerInOwnHalfMap(){
        MapNode currentNode = getCurrentPlayerState().getCurrentPosition();
        return queries.isPlayerInOwnHalfMap(map, currentNode);
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
                ", players=" + (players != null ? players.toString() : "null") +
                ", map=" + map +
                ", treasureCollected=" + treasureCollected +
                ", opponentFortFound=" + opponentFortFound +
                ", treasurePosition=" + treasurePosition +
                ", opponentFortPosition=" + opponentFortPosition +
                '}';
    }
}
