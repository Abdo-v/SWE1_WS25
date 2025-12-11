package client.model.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import client.model.GameState;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.PlayerHalfMap;
import client.model.mapper.Terrain;
import client.model.mapper.OwnToOppMapOrientation;

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
        ArrayList<MapNode> grassNodes = new ArrayList<>();
        if (gameState != null && gameState.getMap() != null) {
            MapNode start = gameState.getCurrentPlayerState() != null ? 
                           gameState.getCurrentPlayerState().getCurrentPosition() : 
                           gameState.getOwnFortPosition();
            PlayerHalfMap ownHalfMap = getArrangedOwnHalfMap(start);
            if (ownHalfMap != null && ownHalfMap.getMapNodes() != null) {
                grassNodes = ownHalfMap.getMapNodes().stream()
                        .filter(node -> node.getTerrain() == Terrain.GRASS)
                        .collect(Collectors.toCollection(ArrayList::new));
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
        LinkedHashMap<MapNode, Boolean> grassTraversal = new LinkedHashMap<>();
        for (MapNode node : grassNodes) {
            grassTraversal.put(node, false);
        }
        return grassTraversal;
    }

    /**
     * Returns a LinkedHashMap of grass nodes in the opponent's half-map, initialized as unvisited.
     * The keys are MapNode objects representing grass nodes, and the values are Booleans indicating if the node has been visited.
     * @return A LinkedHashMap of grass nodes with their visited status.
     */
    public LinkedHashMap<MapNode, Boolean> getTraverseWayForOpponentHalf(){
        ArrayList<MapNode> grassNodes = new ArrayList<>();
        if (gameState != null && gameState.getMap() != null) {
            MapNode start = gameState.getOwnFortPosition();
            if (start == null) {
                System.err.println("WayHelper: Own fort position is null");
            }
            PlayerHalfMap opponentHalfMap = getArrangedOpponentHalfMap(start);
            if (opponentHalfMap != null && opponentHalfMap.getMapNodes() != null) {
                grassNodes = opponentHalfMap.getMapNodes().stream()
                        .filter(node -> node.getTerrain() == Terrain.GRASS)
                        .collect(Collectors.toCollection(ArrayList::new));
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
        LinkedHashMap<MapNode, Boolean> grassTraversal = new LinkedHashMap<>();
        for (MapNode node : grassNodes) {
            grassTraversal.put(node, false);
        }
        return grassTraversal;
    }

    /**
     * Returns a LinkedHashMap of grass nodes in the opponent's half-map from a specific position, initialized as unvisited.
     * @param currentPosition The current position from which to arrange the opponent half-map.
     * @return A LinkedHashMap of grass nodes with their visited status.
     */
    public LinkedHashMap<MapNode, Boolean> getTraverseWayForOpponentHalf(MapNode currentPosition){
        ArrayList<MapNode> grassNodes = new ArrayList<>();
        if (gameState != null && gameState.getMap() != null) {
            PlayerHalfMap opponentHalfMap = getArrangedOpponentHalfMap(currentPosition);
            if (opponentHalfMap != null && opponentHalfMap.getMapNodes() != null) {
                grassNodes = opponentHalfMap.getMapNodes().stream()
                        .filter(node -> node.getTerrain() == Terrain.GRASS)
                        .collect(Collectors.toCollection(ArrayList::new));
            }
        }
        else {
            if(gameState == null) {
                System.err.println("WayHelper: GameState is null, cannot get opponent traversal way.");
            } else {
                System.err.println("WayHelper: GameMap is null, cannot get opponent traversal way.");
            }
        }
        LinkedHashMap<MapNode, Boolean> grassTraversal = new LinkedHashMap<>();
        for (MapNode node : grassNodes) {
            grassTraversal.put(node, false);
        }
        return grassTraversal;
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
            int cost = getCostToReachNode(enemyTruePosition, node);
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
        LinkedHashMap<MapNode, Boolean> mountainFields = new LinkedHashMap<>();
        
        if (gameState != null && gameState.getMap() != null) {
            for (MapNode node : gameState.getMap().getGameMapNodes()) {
                if (node.getTerrain() == Terrain.MOUNTAIN) {
                    mountainFields.put(node, false);
                }
            }
        } else {
            System.err.println("WayHelper: GameState or GameMap is null, cannot get mountain fields.");
        }
        
        return mountainFields;
    }

    /**
     * Returns the arranged own half map based on the current position of the player.
     * The arrangement is done in a Y-snake traversal pattern, starting from the player's current position.
     * 
     * @param currentPosition The current position of the player.
     * @return The arranged PlayerHalfMap containing nodes in Y-snake order.
     */
    public PlayerHalfMap getArrangedOwnHalfMap(MapNode currentPosition){
        GameMap currentMap = gameState.getMap();
        PlayerHalfMap arrangedOwnHalfMap = new PlayerHalfMap();
        if (currentPosition == null) {
            throw new IllegalArgumentException("Current position cannot be null");
        }

        int minScanX, maxScanX, xMidPointThreshold;
        int minScanY, maxScanY, yMidPointThreshold;
        OwnToOppMapOrientation orientation = currentMap.getOrientation();
        
        // Determine the boundaries and midpoints for the player's own half-map
        switch (orientation) {
            case UP_DOWN:
            case LEFT_RIGHT:
                minScanX = 0; maxScanX = 9; xMidPointThreshold = 5;
                minScanY = 0; maxScanY = 4; yMidPointThreshold = 3;
                break;
            case RIGHT_LEFT:
                minScanX = 10; maxScanX = 19; xMidPointThreshold = 15;
                minScanY = 0; maxScanY = 4; yMidPointThreshold = 3;
                break;
            case DOWN_UP:
                minScanX = 0; maxScanX = 9; xMidPointThreshold = 5;
                minScanY = 5; maxScanY = 9; yMidPointThreshold = 8;
                break;
            default:
                throw new IllegalArgumentException("Invalid orientation: " + orientation);
        }

        // Determine X iteration direction (outer loop)
        boolean scanXLeftToRight;
        int currentIterX, endIterX, iterXIncrement;

        if (currentPosition.getX() < xMidPointThreshold) {
            scanXLeftToRight = true;
            currentIterX = minScanX;
            endIterX = maxScanX;
            iterXIncrement = 1;
        } else {
            scanXLeftToRight = false;
            currentIterX = maxScanX;
            endIterX = minScanX;
            iterXIncrement = -1;
        }

        // Determine initial Y iteration direction for the first X column (inner loop)
        boolean currentYScanTopToBottom;
        if (currentPosition.getY() < yMidPointThreshold) {
            currentYScanTopToBottom = true;
        } else {
            currentYScanTopToBottom = false;
        }

        // Y-Snake traversal
        for (int x = currentIterX; (scanXLeftToRight ? x <= endIterX : x >= endIterX); x += iterXIncrement) {
            if (currentYScanTopToBottom) {
                for (int y = minScanY; y <= maxScanY; y++) {
                    MapNode node = currentMap.getNode(x, y);
                    if (node != null) {
                        arrangedOwnHalfMap.addMapNode(node);
                    }
                }
            } else {
                for (int y = maxScanY; y >= minScanY; y--) {
                    MapNode node = currentMap.getNode(x, y);
                    if (node != null) {
                        arrangedOwnHalfMap.addMapNode(node);
                    }
                }
            }
            currentYScanTopToBottom = !currentYScanTopToBottom;
        }
        
        return arrangedOwnHalfMap;
    }

    /**
     * gets the opponent half map arranged from the corner nearest to current (start) position in a Y-snake pattern.
     * The outer loop iterates X, and the inner loop Y, with Y direction alternating.
     * @param currentPosition the current position of the player (in their own half).
     * @return the arranged opponent half map.
     */
    public PlayerHalfMap getArrangedOpponentHalfMap(MapNode currentPosition){
        if (currentPosition == null) {
            throw new IllegalArgumentException("Current position cannot be null");
        }
        GameMap gameMap = gameState.getMap();
        if (gameMap == null) {
            throw new IllegalArgumentException("GameMap cannot be null");
        }
        OwnToOppMapOrientation orientation = gameMap.getOrientation();
        PlayerHalfMap arrangedOpponentHalfMap = new PlayerHalfMap();

        int minOppX, maxOppX; 
        int minOppY, maxOppY; 
        int playerXThreshold; 
        int playerYThreshold; 

        switch (orientation) {
            case DOWN_UP: 
                minOppX = 0; maxOppX = 9;
                minOppY = 0; maxOppY = 4;
                playerXThreshold = 5;
                playerYThreshold = 8;
                break;
            case UP_DOWN: 
                minOppX = 0; maxOppX = 9;
                minOppY = 5; maxOppY = 9;
                playerXThreshold = 5;
                playerYThreshold = 3;
                break;
            case LEFT_RIGHT: 
                minOppX = 10; maxOppX = 19;
                minOppY = 0; maxOppY = 4;
                playerXThreshold = 5;
                playerYThreshold = 3;
                break;
            case RIGHT_LEFT: 
                minOppX = 0; maxOppX = 9;
                minOppY = 0; maxOppY = 4;
                playerXThreshold = 15;
                playerYThreshold = 3;
                break;
            default:
                throw new IllegalArgumentException("Invalid orientation: " + orientation);
        }

        boolean scanXLeftToRight = currentPosition.getX() < playerXThreshold;

        int startXIter, endXIter, iterXIncrement;
        if (scanXLeftToRight) {
            startXIter = minOppX;
            endXIter = maxOppX;
            iterXIncrement = 1;
        } else {
            startXIter = maxOppX;
            endXIter = minOppX;
            iterXIncrement = -1;
        }

        boolean currentYScanTopToBottom = currentPosition.getY() < playerYThreshold;

        for (int x = startXIter; (scanXLeftToRight ? x <= endXIter : x >= endXIter); x += iterXIncrement) {
            if (currentYScanTopToBottom) {
                for (int y = minOppY; y <= maxOppY; y++) {
                    MapNode node = gameMap.getNode(x, y);
                    if (node != null) {
                        arrangedOpponentHalfMap.addMapNode(node);
                    }
                }
            } else {
                for (int y = maxOppY; y >= minOppY; y--) {
                    MapNode node = gameMap.getNode(x, y);
                    if (node != null) {
                        arrangedOpponentHalfMap.addMapNode(node);
                    }
                }
            }
            currentYScanTopToBottom = !currentYScanTopToBottom;
        }
        return arrangedOpponentHalfMap;
    }

    /**
     * Calculates the cost of the shortest path from a start node to a target node
     * using Dijkstra's algorithm and the getMovementCost method for edge weights.
     * Considers only non-diagonal movements.
     *
     * @param startNode The starting MapNode.
     * @param targetNode The target MapNode.
     * @return The total cost of the shortest path, or Integer.MAX_VALUE if no path is found or inputs are invalid.
     */
    private int getCostToReachNode(MapNode startNode, MapNode targetNode) {
        if (startNode == null || targetNode == null || gameState == null || gameState.getMap() == null) {
            return Integer.MAX_VALUE;
        }

        if (startNode.equalsByCoordinates(targetNode)) {
            return 0;
        }

        Map<MapNode, Integer> distances = new HashMap<>();
        Set<MapNode> settledNodes = new HashSet<>();
        GameMap map = gameState.getMap();

        Comparator<MapNode> nodeComparator = Comparator.comparingInt(node -> distances.getOrDefault(node, Integer.MAX_VALUE));
        PriorityQueue<MapNode> pq = new PriorityQueue<>(nodeComparator);

        distances.put(startNode, 0);
        pq.add(startNode);

        while (!pq.isEmpty()) {
            MapNode u = pq.poll();

            if (u.equalsByCoordinates(targetNode)) {
                return distances.get(u);
            }

            if (settledNodes.contains(u) || distances.getOrDefault(u, Integer.MAX_VALUE) == Integer.MAX_VALUE) {
                continue;
            }
            settledNodes.add(u);

            int currentX = u.getX();
            int currentY = u.getY();
            int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

            for (int[] dir : directions) {
                int newX = currentX + dir[0];
                int newY = currentY + dir[1];

                try {
                    MapNode neighbor = map.getNode(newX, newY);
                    if (neighbor == null || settledNodes.contains(neighbor)) {
                        continue;
                    }

                    int edgeCost = getMovementCost(u, neighbor);
                    if (edgeCost == Integer.MAX_VALUE) {
                        continue;
                    }

                    int currentDistanceU = distances.get(u);
                    int newDist = currentDistanceU + edgeCost;
                    int oldDist = distances.getOrDefault(neighbor, Integer.MAX_VALUE);

                    if (newDist < oldDist) {
                        distances.put(neighbor, newDist);
                        pq.remove(neighbor);
                        pq.add(neighbor);
                    }
                } catch (IllegalArgumentException e) {
                    // Out of bounds
                }
            }
        }

        return Integer.MAX_VALUE;
    }

    /**
     * Calculates the movement cost between two adjacent MapNodes based on their terrain types.
     * @param from The starting MapNode.
     * @param to The target MapNode.
     * @return The movement cost between the two MapNodes.
     */
    private int getMovementCost(MapNode from, MapNode to) {
        if (to.getTerrain() == Terrain.WATER) {
            return Integer.MAX_VALUE;
        }

        Terrain fromTerrain = from.getTerrain();
        Terrain toTerrain = to.getTerrain();

        if (fromTerrain == Terrain.GRASS && toTerrain == Terrain.GRASS) return 1;
        if (fromTerrain == Terrain.GRASS && toTerrain == Terrain.MOUNTAIN) return 2;
        if (fromTerrain == Terrain.MOUNTAIN && toTerrain == Terrain.GRASS) return 1;
        if (fromTerrain == Terrain.MOUNTAIN && toTerrain == Terrain.MOUNTAIN) return 2;

        System.err.println("WayHelper.getMovementCost: Unhandled terrain transition from " + fromTerrain + " to " + toTerrain);
        return Integer.MAX_VALUE;
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
