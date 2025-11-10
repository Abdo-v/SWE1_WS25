package client.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.Terrain;

import java.util.ArrayList;

/**
 * Class representing the current state of the game.
 */
public class GameState implements client.observer.util.Observable {
    private static final Logger logger = LoggerFactory.getLogger(GameState.class);
    
    private final String gameStateID;
    private ArrayList<PlayerState> players;
    private GameMap map;
    private ArrayList<client.observer.util.Observer> observers = new ArrayList<>();
    private boolean treasureCollected = false;
    private boolean opponentFortFound = false;
    private MapNode treasurePosition = null;
    private MapNode opponentFortPosition = null; 


    @Override
    public void addObserver(client.observer.util.Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            logger.debug("Observer {} added to GameState. Total observers: {}", 
                        observer.getClass().getSimpleName(), observers.size());
            // Notify the newly added observer immediately with current state
            try {
                observer.update(this);
                logger.trace("Newly added observer {} notified of current state", 
                           observer.getClass().getSimpleName());
            } catch (Exception e) {
                logger.error("Exception occurred while notifying newly added observer {}: {}", 
                           observer.getClass().getSimpleName(), e.getMessage(), e);
            }
        } else {
            logger.trace("Observer {} already registered, skipping duplicate", 
                        observer.getClass().getSimpleName());
        }
    }

    @Override
    public void removeObserver(client.observer.util.Observer observer) {
        boolean removed = observers.remove(observer);
        if (removed) {
            logger.debug("Observer {} removed from GameState. Total observers: {}", 
                        observer.getClass().getSimpleName(), observers.size());
        } else {
            logger.trace("Observer {} was not found for removal", 
                        observer.getClass().getSimpleName());
        }
    }

    @Override
    public void notifyObservers() {
        logger.trace("Notifying {} observers of GameState change", observers.size());
        // Use a copy to avoid ConcurrentModificationException if observers modify the list
        for (client.observer.util.Observer observer : new ArrayList<>(observers)) {
            try {
                logger.trace("Notifying observer: {}", observer.getClass().getSimpleName());
                observer.update(this);
            } catch (Exception e) {
                logger.error("Exception occurred while notifying observer {}: {}", 
                           observer.getClass().getSimpleName(), e.getMessage(), e);
                // Continue notifying other observers despite this exception
            }
        }
        logger.trace("All observers notified successfully");
    }
    
    /**
     * Constructs a new GameState with the given ID and players.
     * 
     * @param ID The game state ID
     * @param players Array of player states
     */
    public GameState(String ID, ArrayList<PlayerState> players) {
        this.gameStateID = ID;
        this.players = players;
        this.treasureCollected = false;
        this.opponentFortFound = false;
    }

    /**
     * Constructs a new GameState with the given ID.
     * 
     * @param ID The game state ID
     */
    public GameState(String ID) {
        this.gameStateID = ID;
        this.treasureCollected = false;
        this.opponentFortFound = false;
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
    }

    public GameState() {
        this.gameStateID = "default";
        this.players = new ArrayList<>();
        this.map = null;
        this.treasureCollected = false;
        this.opponentFortFound = false;
    }

    
    /**
     * Updates this game state with data from another game state.
     * 
     * @param gameState The game state to update from
     */
    public void updateGameState(GameState gameState) {
        logger.debug("Updating GameState with new data");
        //System.out.print("notify called: ");
        if (gameState != null) {
            this.players = gameState.getPlayers();
            
            this.map = gameState.getMap();
            this.treasureCollected = gameState.isTreasureCollected();
            this.opponentFortFound = gameState.isOpponentFortFound();
            this.treasurePosition = gameState.getTreasurePosition();
            this.opponentFortPosition = gameState.getOpponentFortPosition();
            logger.trace("GameState updated - treasure collected: {}, opponent fort found: {}", 
                        treasureCollected, opponentFortFound);
        } else {
            logger.warn("Attempted to update GameState with null gameState");
        }

        logger.debug("GameState update complete, notifying {} observers", observers.size());
        notifyObservers();
        //System.out.println();
    }
    
    /**
     * Process vision for the current player position based on terrain type.
     * This method should be called whenever the player moves to a new position.
     * 
     * @param currentPosition The current position of the player
     */
    public void processVision(MapNode currentPosition) {
        if (currentPosition == null || map == null) return;
        
        Terrain terrain = currentPosition.getTerrain();
        checkForDiscoveries(currentPosition);
        
        if (terrain == Terrain.MOUNTAIN) {
            processExtendedVision(currentPosition);
        }
    }
    
    /**
     * Process extended vision provided by mountain tiles
     * 
     * @param center The center tile (mountain) from which to process vision
     */
    private void processExtendedVision(MapNode center) {
        int x = center.getX();
        int y = center.getY();
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                try {
                    MapNode node = map.getNode(x + dx, y + dy);
                    if (node != null) {
                        checkForDiscoveries(node);
                    }
                } catch (IllegalArgumentException e) {
                }
            }
        }
    }
    
    /**
     * Check a specific map node for treasure or fort
     * 
     * @param node The map node to check
     */
    private void checkForDiscoveries(MapNode node) {
        if (!opponentFortFound && isOpponentFortAtNode(node)) {
            opponentFortFound = true;
            opponentFortPosition = node;
        }
    }
    
    /**
     * Check if a node contains the opponent's fort
     * This is a placeholder and should be implemented based on the game's logic
     */
    private boolean isOpponentFortAtNode(MapNode node) {
        if (node != null && node.isFortPresent()) {
            return true;
        }
        return false;
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
            logger.info("Opponent fort position set at: {}", opponentFortPosition.printCoordinates());
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
        logger.debug("Players list updated with {} players", players != null ? players.size() : 0);
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
        logger.debug("Game map updated - size: {}", map != null ? map.getContentSize() : 0);
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
        logger.info("Treasure collected status changed to: {}", treasureCollected);
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
        logger.info("Opponent fort found status changed to: {}", opponentFortFound);
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
        if (treasurePosition != null) {
            logger.info("Treasure position set at: {}", treasurePosition.printCoordinates());
        } else {
            logger.debug("Treasure position cleared");
        }
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
        for (MapNode node : map.getOwnHalfMap().getMapNodes()) {
            if (node.isFortPresent()) {
                return node;
            }
        }
        return null;
    }

    /**
     * Gets the opponent's fort position.
     * 
     * @return The opponent's fort position.
     */
    public MapNode getEnemyFortPosition(){
        for (MapNode node : map.getOpponentHalfMap().getMapNodes()) {
            if (node.isFortPresent()) {
                return node;
            }
        }
        return null;
    }

    public MapNode getEnemyCurrentPosition(){
        if (getPlayers() == null || getPlayers().size() < 2) {
            return null; // No enemy player available
        }
        return getPlayers().get(1).getCurrentPosition();
    }

    public PlayerState getEnemyPlayerState(){
        if (getPlayers() == null || getPlayers().size() < 2) {
            return null;
        }
        return getPlayers().get(1);
    }

    /**
     * Checks if the player is in their own half of the map.
     * uses getCurrentPlayerState().getCurrentPosition() and and getOrientation().
     * @return true if the player is in their own half, false otherwise.
     */

    public boolean isPlayerInOwnHalfMap(){
        MapNode currentNode = getCurrentPlayerState().getCurrentPosition();
        switch (map.getOrientation())
        {
            case UP_DOWN:
                return currentNode.getY() <= 4;
            case DOWN_UP:
                return currentNode.getY() >= 5;
            case LEFT_RIGHT:
                return currentNode.getX() <= 9;
            case RIGHT_LEFT:
                return currentNode.getX() >= 10;
            default:
                return false;
        }
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
