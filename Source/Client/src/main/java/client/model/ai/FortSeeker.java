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

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

import client.model.GameState;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.PlayerHalfMap;
import client.model.mapper.Terrain;
import client.model.mapper.OwnToOppMapOrientation;

public class FortSeeker implements client.observer.util.Observer {

    // private static final Logger logger = LoggerFactory.getLogger(FortSeeker.class);

    private GameState gameState;
    private boolean enemyFortFound = false;

    public FortSeeker(GameState gameState) {
        this.gameState = gameState;

    }

    public FortSeeker() {
        this.gameState = null;
    }
    /**
     * Returns a LinkedHashMap of grass nodes in the opponent's half-map, initialized as unvisited.
     * The keys are MapNode objects representing grass nodes, and the values are Booleans indicating if the node has been visited.
     * @return A LinkedHashMap of grass nodes with their visited status.
     */
    public LinkedHashMap<MapNode, Boolean> getTraverseWay(){
        // logger.debug("Generating traversal way for opponent half-map");
        ArrayList<MapNode> grassNodes = new ArrayList<>();
        if (gameState != null && gameState.getMap() != null) {
            MapNode start = gameState.getOwnFortPosition();
            if (start == null) {
                throw new IllegalArgumentException("Own fort position cannot be null");
            }
            PlayerHalfMap opponentHalfMap = getArrangedOpponentHalfMap(start);
            if (opponentHalfMap != null && opponentHalfMap.getMapNodes() != null) {
                grassNodes = opponentHalfMap.getMapNodes().stream()
                        .filter(node -> node.getTerrain() == Terrain.GRASS)
                        .collect(Collectors.toCollection(ArrayList::new));
                // logger.debug("Found {} grass nodes in arranged opponent half-map", grassNodes.size());
            } else {
                // logger.warn("Opponent half-map or its nodes are null");
            }
        }
        else {
            if(gameState == null) {
                // logger.error("GameState is null, cannot get traversal way");
                System.err.println("FortSeeker: GameState is null, cannot get traversal way.");
                new Throwable("FortSeeker: GameState is null, cannot get traversal way.").printStackTrace();
            } else if (gameState.getMap() == null) {
                // logger.error("GameMap is null, cannot get traversal way");
                System.err.println("FortSeeker: GameMap is null, cannot get traversal way.");
            } else {
                // logger.warn("No grass nodes found in the specified half-map");
                System.err.println("FortSeeker: No grass nodes found in the specified half-map.");  
            }
        }
        LinkedHashMap<MapNode, Boolean> grassTraversal = new LinkedHashMap<>();
        for (MapNode node : grassNodes) {
            grassTraversal.put(node, false);
        }
        // logger.debug("Initialized opponent traversal way with {} grass nodes (all unvisited)", grassTraversal.size());
        return grassTraversal;
    }

    public MapNode getEnemyFortNodeIfFound() {
        // logger.trace("Searching for enemy fort in opponent half-map");
        MapNode res = null;
        if (gameState == null || gameState.getMap() == null) {
            // logger.warn("Cannot search for enemy fort - GameState or map is null");
            return null;
        }
        if (gameState.getCurrentPlayerState() == null) {
            throw new IllegalArgumentException("Current player state cannot be null");
        }
        for (MapNode node : gameState.getMap().getOpponentHalfMap().getMapNodes()) {
            if (node.isFortPresent()) {
                res = node;
                // logger.info("Enemy fort found at position: {}", res.printCoordinates());
                break;
            }
        }
        if(res != null) {
            // logger.debug("Enemy fort confirmed at: {}, current position: {}", 
            //             res.printCoordinates(),
            //             gameState.getCurrentPlayerState().getCurrentPosition().printCoordinates());
        } else {
            // logger.trace("No enemy fort found in opponent half-map");
        }
        return res;
    }

    public LinkedHashMap<MapNode, Boolean> getTraverseWay(MapNode currentPosition){
        // logger.debug("Generating traversal way for opponent half-map from position: {}", 
        //             currentPosition != null ? currentPosition.printCoordinates() : "null");
        ArrayList<MapNode> grassNodes = new ArrayList<>();
        if (gameState != null && gameState.getMap() != null) {
            PlayerHalfMap opponentHalfMap = getArrangedOpponentHalfMap(currentPosition);
            if (opponentHalfMap != null && opponentHalfMap.getMapNodes() != null) {
                grassNodes = opponentHalfMap.getMapNodes().stream()
                        .filter(node -> node.getTerrain() == Terrain.GRASS)
                        .collect(Collectors.toCollection(ArrayList::new));
                // logger.debug("Found {} grass nodes from position-specific arrangement", grassNodes.size());
            }
        }
        else {
            if(gameState == null) {
                // logger.error("GameState is null, cannot get traversal way");
                System.err.println("FortSeeker: GameState is null, cannot get traversal way.");
                new Throwable("FortSeeker: GameState is null, cannot get traversal way.").printStackTrace();
            } else if (gameState.getMap() == null) {
                // logger.error("GameMap is null, cannot get traversal way");
                System.err.println("FortSeeker: GameMap is null, cannot get traversal way.");
            } else {
                // logger.warn("No grass nodes found in the specified half-map");
                System.err.println("FortSeeker: No grass nodes found in the specified half-map.");  
            }
        }
        LinkedHashMap<MapNode, Boolean> grassTraversal = new LinkedHashMap<>();
        for (MapNode node : grassNodes) {
            grassTraversal.put(node, false);
        }
        // logger.debug("Generated position-specific traversal way with {} nodes", grassTraversal.size());
        return grassTraversal;
    }

    public LinkedHashMap<MapNode, Boolean> getFilteredTraverseWay(MapNode enemyTruePosition){
        // logger.info("Filtering traversal way based on enemy position: {}", 
        //            enemyTruePosition != null ? enemyTruePosition.printCoordinates() : "null");
        if (enemyTruePosition == null) {
            // logger.error("Enemy true position cannot be null for filtering");
            throw new IllegalArgumentException("Enemy true position cannot be null");
        }
        if (gameState == null || gameState.getCurrentPlayerState() == null) {
            throw new IllegalArgumentException("GameState or current player state cannot be null");
        }
        MapNode currentPosition = gameState.getCurrentPlayerState().getCurrentPosition();
        LinkedHashMap<MapNode, Boolean> fullTraversalWay = getTraverseWay(currentPosition);
        LinkedHashMap<MapNode, Boolean> filteredTraversalWay = new LinkedHashMap<>();
        // logger.debug("Filtering {} nodes based on 8-move reachability from enemy position", 
        //             fullTraversalWay.size());
        int reachableCount = 0;
        int unreachableCount = 0;
        for (MapNode node : fullTraversalWay.keySet()) {
            if (node == null) {
                // logger.warn("Skipping null node in traversal way");
                System.err.println("FortSeeker: Node is null, skipping.");
                continue;
            }
            int cost = getCostToReachNode(enemyTruePosition, node);
            if (cost <= 8) {
                filteredTraversalWay.put(node, fullTraversalWay.get(node));
                reachableCount++;
            } else {
                unreachableCount++;
            }
        }
        // logger.info("Filtered traversal way: {} reachable nodes, {} filtered out (unreachable in 8 moves)", 
        //            reachableCount, unreachableCount);
        return filteredTraversalWay;
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
            // logger.error("Invalid parameters for cost calculation - start: {}, target: {}, gameState: {}, map: {}", 
            //             startNode != null, targetNode != null, gameState != null, 
            //             gameState != null ? gameState.getMap() != null : false);
            System.err.println("FortSeeker.getCostToReachNode: Start, target, gameState, or map is null.");
            return Integer.MAX_VALUE;
        }

        if (startNode.equalsByCoordinates(targetNode)) {
            // logger.trace("Start and target are the same, cost is 0");
            return 0; // No cost to stay in place
        }

        // logger.trace("Calculating path cost from {} to {}", 
        //             startNode.printCoordinates(), targetNode.printCoordinates());

        Map<MapNode, Integer> distances = new HashMap<>();
        Set<MapNode> settledNodes = new HashSet<>();
        GameMap map = gameState.getMap();

        // Comparator for the PriorityQueue based on current known distances
        Comparator<MapNode> nodeComparator = Comparator.comparingInt(node -> distances.getOrDefault(node, Integer.MAX_VALUE));
        PriorityQueue<MapNode> pq = new PriorityQueue<>(nodeComparator);

        // Initialize distances: 0 for start, infinity for others
        distances.put(startNode, 0);
        pq.add(startNode);

        int exploredNodes = 0;
        while (!pq.isEmpty()) {
            MapNode u = pq.poll();
            exploredNodes++;

            // If target is polled from PQ, its shortest distance is found
            if (u.equalsByCoordinates(targetNode)) {
                int finalCost = distances.get(u);
                // logger.trace("Path found from {} to {} with cost {} (explored {} nodes)", 
                //            startNode.printCoordinates(), targetNode.printCoordinates(), 
                //            finalCost, exploredNodes);
                return finalCost; // Target found, return its distance
            }

            // If already settled or unreachable, skip
            if (settledNodes.contains(u) || distances.getOrDefault(u, Integer.MAX_VALUE) == Integer.MAX_VALUE) {
                continue;
            }
            settledNodes.add(u);

            // Explore neighbors (Up, Down, Left, Right)
            int[] dx = {0, 0, 1, -1}; // Changes in x for Right, Left
            int[] dy = {1, -1, 0, 0}; // Changes in y for Down, Up

            for (int i = 0; i < 4; i++) {
                int neighborX = u.getX() + dx[i];
                int neighborY = u.getY() + dy[i];

                MapNode v = null;
                try {
                    // Check bounds before getting node
                    if (neighborX >= 0 && neighborX <= map.getMaxX() && neighborY >= 0 && neighborY <= map.getMaxY()) {
                        v = map.getNode(neighborX, neighborY);
                    }
                } catch (IllegalArgumentException e) {
                    // Node is out of bounds or invalid, v remains null
                }
                

                if (v != null && !settledNodes.contains(v)) {
                    int costUV = getMovementCost(u, v); // Use FortSeeker's getMovementCost
                    if (costUV == Integer.MAX_VALUE) { // Impassable neighbor
                        continue;
                    }

                    int distanceU = distances.get(u); 
                    int newDistToV = distanceU + costUV;

                    if (newDistToV < distances.getOrDefault(v, Integer.MAX_VALUE)) {
                        distances.put(v, newDistToV);
                        pq.remove(v); // Remove if it exists to update priority
                        pq.add(v);    // Add with updated distance
                    }
                }
            }
        }

        // Target not reached if loop finishes without returning
        // logger.debug("No path found from {} to {} (explored {} nodes)", 
        //             startNode.printCoordinates(), targetNode.printCoordinates(), exploredNodes);
        return Integer.MAX_VALUE; // Indicate unreachable
    }
    /**
     * Calculates the movement cost between two adjacent MapNodes based on their terrain types.
     * @param from The starting MapNode.
     * @param to The target MapNode.
     * @return The movement cost between the two MapNodes.
     */
    private int getMovementCost(MapNode from, MapNode to) {
        if (to.getTerrain() == Terrain.WATER) {
            return Integer.MAX_VALUE; // Impassable
        }

        Terrain fromTerrain = from.getTerrain();
        Terrain toTerrain = to.getTerrain();

        if (fromTerrain == Terrain.GRASS && toTerrain == Terrain.GRASS) return 2;
        if (fromTerrain == Terrain.GRASS && toTerrain == Terrain.MOUNTAIN) return 3;
        if (fromTerrain == Terrain.MOUNTAIN && toTerrain == Terrain.GRASS) return 3;
        if (fromTerrain == Terrain.MOUNTAIN && toTerrain == Terrain.MOUNTAIN) return 4;

        System.err.println("WayFinder.getMovementCost: Unhandled terrain transition from " + fromTerrain + " to " + toTerrain);
        return Integer.MAX_VALUE;
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
                playerXThreshold = minOppX + (maxOppX - minOppX + 1) / 2;
                playerYThreshold = minOppY + (maxOppY - minOppY + 1) / 2;
                break;
            case UP_DOWN: 
                minOppX = 0; maxOppX = 9;
                minOppY = 5; maxOppY = 9;
                playerXThreshold = minOppX + (maxOppX - minOppX + 1) / 2;
                playerYThreshold = minOppY + (maxOppY - minOppY + 1) / 2;
                break;
            case LEFT_RIGHT: 
                minOppX = 10; maxOppX = 19;
                minOppY = 0; maxOppY = 4;
                playerXThreshold = minOppX + (maxOppX - minOppX + 1) / 2;
                playerYThreshold = minOppY + (maxOppY - minOppY + 1) / 2;
                break;
            case RIGHT_LEFT: 
                minOppX = 0; maxOppX = 9;
                minOppY = 0; maxOppY = 4;
                playerXThreshold = minOppX + (maxOppX - minOppX + 1) / 2;
                playerYThreshold = minOppY + (maxOppY - minOppY + 1) / 2;
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
                    if (node != null) arrangedOpponentHalfMap.addMapNode(node);
                }
            } else {
                for (int y = maxOppY; y >= minOppY; y--) {
                    MapNode node = gameMap.getNode(x, y);
                    if (node != null) arrangedOpponentHalfMap.addMapNode(node);
                }
            }
            currentYScanTopToBottom = !currentYScanTopToBottom;
        }
        return arrangedOpponentHalfMap;
    }

    @Override
    public void update(GameState gameState) {
        // logger.trace("FortSeeker received GameState update");
        this.gameState = gameState;
        
        if (gameState.getOpponentFortPosition() != null && !enemyFortFound) {
            enemyFortFound = true;
            // logger.info("Enemy fort discovered at position: {}", gameState.getOpponentFortPosition().printCoordinates());
        }
    }
    // for testing purposes, TDD
	public Object getGameState() {
		return gameState;
	}



}
