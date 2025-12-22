package client.model.ai;

import java.util.LinkedHashMap;

import client.model.GameState;
import client.model.mapper.MapNode;
import client.model.mapper.PlayerHalfMap;

/**
 * Helper class for managing traversal paths and half-map arrangements.
 * This class encapsulates logic for organizing and tracking map nodes for traversal strategies.
 */
public class WayHelper implements client.observer.util.Observer {

    private GameState gameState;
    private LinkedHashMap<MapNode, Boolean> halfMapVisitedGrassFields;
    private LinkedHashMap<MapNode, Boolean> oppHalfMapVisitedGrassFields;
    private LinkedHashMap<MapNode, Boolean> allMountainFields;

    /**
     * Constructs a WayHelper with a given GameState.
     * @param gameState The current game state.
     */
    public WayHelper(GameState gameState) {
        this.gameState = gameState;
        this.halfMapVisitedGrassFields = new LinkedHashMap<>();
        this.oppHalfMapVisitedGrassFields = new LinkedHashMap<>();
        this.allMountainFields = new LinkedHashMap<>();
    }

    /**
     * Default constructor for WayHelper.
     */
    public WayHelper() {
        this.gameState = null;
        this.halfMapVisitedGrassFields = new LinkedHashMap<>();
        this.oppHalfMapVisitedGrassFields = new LinkedHashMap<>();
        this.allMountainFields = new LinkedHashMap<>();
    }

    /**
     * Returns a LinkedHashMap of grass nodes in the player's own half-map, initialized as unvisited.
     * The keys are MapNode objects representing grass nodes, and the values are Booleans indicating whether the node has been visited.
     * 
     * @return A LinkedHashMap of grass nodes with their visited status.
     */
    public LinkedHashMap<MapNode, Boolean> getTraverseWayForOwnHalf(){
        if (gameState != null && gameState.getMap() != null) {
            MapNode start = gameState.getCurrentPlayerState() != null ? 
                           gameState.getCurrentPlayerState().getCurrentPosition() : 
                           gameState.getOwnFortPosition();
            PlayerHalfMap ownHalfMap = getArrangedOwnHalfMap(start);
            if (ownHalfMap != null && ownHalfMap.getMapNodes() != null) {
                return TraversalWayBuilders.grassTraversal(ownHalfMap);
            } else {
                System.err.println("WayHelper: Own half-map or its nodes are null");
            }
        }
        else{
            if(gameState == null) {
                System.err.println("WayHelper: GameState is null, cannot get traversal way.");
                new Throwable("WayHelper: GameState is null, cannot get traversal way.").printStackTrace();
            } else if (gameState.getMap() == null) {
                System.err.println("WayHelper: GameMap is null, cannot get traversal way.");
            } else {
                System.err.println("WayHelper: No grass nodes found in the specified half-map.");
            }
        }
        return new LinkedHashMap<>();
    }

    /**
     * Returns a LinkedHashMap of grass nodes in the opponent's half-map, initialized as unvisited.
     * The keys are MapNode objects representing grass nodes, and the values are Booleans indicating if the node has been visited.
     * @return A LinkedHashMap of grass nodes with their visited status.
     */
    public LinkedHashMap<MapNode, Boolean> getTraverseWayForOpponentHalf(){
        if (gameState != null && gameState.getMap() != null) {
            MapNode start = gameState.getOwnFortPosition();
            if (start == null) {
                System.err.println("WayHelper: Own fort position is null");
            }
            PlayerHalfMap opponentHalfMap = getArrangedOpponentHalfMap(start);
            if (opponentHalfMap != null && opponentHalfMap.getMapNodes() != null) {
                return TraversalWayBuilders.grassTraversal(opponentHalfMap);
            } else {
                System.err.println("WayHelper: Opponent half-map or its nodes are null");
            }
        }
        else {
            if(gameState == null) {
                System.err.println("WayHelper: GameState is null, cannot get opponent traversal way.");
            } else {
                System.err.println("WayHelper: GameMap is null, cannot get opponent traversal way.");
            }
        }
        return new LinkedHashMap<>();
    }

    /**
     * Returns a LinkedHashMap of grass nodes in the opponent's half-map from a specific position, initialized as unvisited.
     * @param currentPosition The current position from which to arrange the opponent half-map.
     * @return A LinkedHashMap of grass nodes with their visited status.
     */
    public LinkedHashMap<MapNode, Boolean> getTraverseWayForOpponentHalf(MapNode currentPosition){
        if (gameState != null && gameState.getMap() != null) {
            PlayerHalfMap opponentHalfMap = getArrangedOpponentHalfMap(currentPosition);
            if (opponentHalfMap != null && opponentHalfMap.getMapNodes() != null) {
                return TraversalWayBuilders.grassTraversal(opponentHalfMap);
            }
        }
        else {
            if(gameState == null) {
                System.err.println("WayHelper: GameState is null, cannot get opponent traversal way.");
            } else {
                System.err.println("WayHelper: GameMap is null, cannot get opponent traversal way.");
            }
        }
        return new LinkedHashMap<>();
    }

    /**
     * Filters the traversal way based on enemy position, keeping only nodes reachable within 8 moves.
     * @param enemyTruePosition The enemy's true position.
     * @return A filtered LinkedHashMap containing only reachable nodes.
     */
    public LinkedHashMap<MapNode, Boolean> getFilteredTraverseWay(MapNode enemyTruePosition){
        if (enemyTruePosition == null) {
            throw new IllegalArgumentException("Enemy true position cannot be null");
        }
        if (gameState == null || gameState.getCurrentPlayerState() == null) {
            throw new IllegalArgumentException("GameState or current player state cannot be null");
        }
        MapNode currentPosition = gameState.getCurrentPlayerState().getCurrentPosition();
        LinkedHashMap<MapNode, Boolean> fullTraversalWay = getTraverseWayForOpponentHalf(currentPosition);
        LinkedHashMap<MapNode, Boolean> filteredTraversalWay = new LinkedHashMap<>();
        
        for (MapNode node : fullTraversalWay.keySet()) {
            if (node == null) {
                continue;
            }
            int cost = GridDijkstra.shortestPathCost(gameState.getMap(), enemyTruePosition, node, MovementCostProfile.WAY_HELPER);
            if (cost <= 8) {
                filteredTraversalWay.put(node, false);
            }
        }
        return filteredTraversalWay;
    }

    /**
     * Returns a LinkedHashMap of all mountain fields in the game map.
     * The keys are MapNode objects representing the mountain nodes,
     * and the values are initialized to false (indicating unvisited).
     * @return A LinkedHashMap containing all mountain fields.
     */
    public LinkedHashMap<MapNode,Boolean> getAllMountainFields(){
        if (gameState != null && gameState.getMap() != null) {
            return TraversalWayBuilders.mountainFields(gameState.getMap());
        }
        System.err.println("WayHelper: GameState or GameMap is null, cannot get mountain fields.");
        return new LinkedHashMap<>();
    }

    /**
     * Returns the arranged own half map based on the current position of the player.
     * The arrangement is done in a Y-snake traversal pattern, starting from the player's current position.
     * 
     * @param currentPosition The current position of the player.
     * @return The arranged PlayerHalfMap containing nodes in Y-snake order.
     */
    public PlayerHalfMap getArrangedOwnHalfMap(MapNode currentPosition){
        if (gameState == null || gameState.getMap() == null) {
            throw new IllegalArgumentException("GameState/GameMap cannot be null");
        }
        return HalfMapSnakeArranger.arrangeOwnHalf(gameState.getMap(), currentPosition);
    }

    /**
     * gets the opponent half map arranged from the corner nearest to current (start) position in a Y-snake pattern.
     * The outer loop iterates X, and the inner loop Y, with Y direction alternating.
     * @param currentPosition the current position of the player (in their own half).
     * @return the arranged opponent half map.
     */
    public PlayerHalfMap getArrangedOpponentHalfMap(MapNode currentPosition){
        if (gameState == null || gameState.getMap() == null) {
            throw new IllegalArgumentException("GameState/GameMap cannot be null");
        }
        return HalfMapSnakeArranger.arrangeOpponentHalf(gameState.getMap(), currentPosition);
    }

    @Override
    public void update(GameState gameState) {
        this.gameState = gameState;
    }

    // Getters for the fields
    public LinkedHashMap<MapNode, Boolean> getHalfMapVisitedGrassFields() {
        return halfMapVisitedGrassFields;
    }

    public LinkedHashMap<MapNode, Boolean> getOppHalfMapVisitedGrassFields() {
        return oppHalfMapVisitedGrassFields;
    }

    public LinkedHashMap<MapNode, Boolean> getAllMountainFieldsMap() {
        return allMountainFields;
    }

    public void setHalfMapVisitedGrassFields(LinkedHashMap<MapNode, Boolean> halfMapVisitedGrassFields) {
        this.halfMapVisitedGrassFields = halfMapVisitedGrassFields;
    }

    public void setOppHalfMapVisitedGrassFields(LinkedHashMap<MapNode, Boolean> oppHalfMapVisitedGrassFields) {
        this.oppHalfMapVisitedGrassFields = oppHalfMapVisitedGrassFields;
    }

    public void setAllMountainFields(LinkedHashMap<MapNode, Boolean> allMountainFields) {
        this.allMountainFields = allMountainFields;
    }

    public GameState getGameState() {
        return gameState;
    }
}
