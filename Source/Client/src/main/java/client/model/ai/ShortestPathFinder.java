package client.model.ai;

import java.util.*;

import client.model.Direction;
import client.model.GameState;
import client.model.mapper.MapNode;
import client.model.mapper.Terrain;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

public class ShortestPathFinder implements client.observer.util.Observer {

    // private static final Logger logger = LoggerFactory.getLogger(ShortestPathFinder.class);
    private GameState gameState;

    public ShortestPathFinder(GameState gameState) {
        this.gameState = gameState;
    }

    public ShortestPathFinder() {
        this.gameState = null;
    }

    /**
     * Finds the next valid node to move towards the target.
     * @return The Direction to move towards the target node, or null if no valid move is found.
     */
    public Direction findNextValidNodeToTarget(MapNode targetMapNode){
        // logger.debug("Finding next valid node to target: {}", 
        //             targetMapNode != null ? targetMapNode.printCoordinates() : "null");
        
        MapNode target = targetMapNode;
        MapNode current = gameState.getCurrentPlayerState().getCurrentPosition();
        
        if(current == null || target == null) {
            // logger.error("Current or target position is null - current: {}, target: {}", 
            //             current != null, target != null);
            System.err.println("WayFinder: Current or Target position is null, cannot find next valid direction.");
            throw new IllegalStateException("Current and Target position must be set before finding next valid direction.");
        }
        
        // logger.trace("Pathfinding from {} to {}", current.printCoordinates(), target.printCoordinates());
        ArrayList<MapNode> path = findShortestPath(current, target);
        
        if (path.isEmpty() || path == null) {
            // logger.warn("No valid path found to target {}", target.printCoordinates());
            System.out.println("WayFinder: No valid path found to target node: path is empty.");
            return null; // No valid path to target
        }
        
        // logger.trace("Path found with {} nodes", path.size());
        
        for(MapNode node : path) {
            if (node.equalsByCoordinates(target)) {
                // logger.error("Already at target node {} - this should not happen during pathfinding", 
                //            target.printCoordinates());
                System.err.println("WayFinder: Reached target node: target should be updated before finding next move.");
                //print own player state
                System.out.println("WayFinder: Current Player State: " + gameState.getCurrentPlayerState().toString());
                new Throwable("WayFinder: Reached target node, but companion should be updated before finding next move.").printStackTrace();
            }
            if( node.equalsByCoordinates(current)) {
                // call getDirectionToNeighbor to get the direction to the next node in the path
                if (path.indexOf(node) + 1 < path.size()) {
                    MapNode nextNode = path.get(path.indexOf(node) + 1);
                    // logger.debug("Next node in path: {}", nextNode.printCoordinates());
                    //System.out.println(nextNode.toString()); // Commented out for debugging
                    //updateCompaionNext(nextNode);
                    //nextMapNode = nextNode;
                    Direction direction = getDirectionToNeighbor(nextNode);
                    // logger.info("Direction to next node: {}", direction);
                    return direction;
                } else {
                    // logger.warn("Current node is the last in the path, no next node to move towards");
                    System.out.println("WayFinder: Current node is the last in the path, no next node to move towards.");
                    return null; // No next node to move towards
                }
            }
        }
        // logger.warn("Current position {} not found in calculated path", current.printCoordinates());
        return null; // If we reach here, something went wrong
    }

    /**
     * Finds the shortest path from a start node to a target node using Dijkstra's algorithm.
     * The path is a list of MapNode objects, including the start and target nodes.
     * Movement costs are determined by getMovementCost().
     *
     * @param start  The starting MapNode.
     * @param target The target MapNode.
     * @return An ArrayList of MapNode objects representing the shortest path from start to target.
     *         Returns an empty list if no path is found, or if start/target is null or invalid.
     *         If start and target are the same, returns a list containing just the start node.
     */
    public ArrayList<MapNode> findShortestPath(MapNode start, MapNode target) {
        // logger.debug("Computing shortest path from {} to {} using Dijkstra's algorithm", 
        //             start != null ? start.printCoordinates() : "null",
        //             target != null ? target.printCoordinates() : "null");
        
        ArrayList<MapNode> path = new ArrayList<>();
        if (start == null || target == null || gameState == null || gameState.getMap() == null) {
            // logger.error("Invalid pathfinding parameters - start: {}, target: {}, gameState: {}, map: {}", 
            //             start != null, target != null, gameState != null, 
            //             gameState != null ? gameState.getMap() != null : false);
            System.err.println("WayFinder.findShortestPath (Dijkstra): Start, target, gameState, or map is null.");
            return path; // Return empty path
        }

        if (start.equalsByCoordinates(target)) {
            // logger.debug("Start and target are the same, returning single-node path");
            path.add(start);
            return path;
        }

        Map<MapNode, Integer> distances = new HashMap<>();
        Map<MapNode, MapNode> predecessors = new HashMap<>();
        Set<MapNode> settledNodes = new HashSet<>();

        // Comparator for the PriorityQueue based on current known distances
        Comparator<MapNode> nodeComparator = Comparator.comparingInt(node -> distances.getOrDefault(node, Integer.MAX_VALUE));
        PriorityQueue<MapNode> pq = new PriorityQueue<>(nodeComparator);

        // Initialize distances: 0 for start, infinity for others
        distances.put(start, 0);
        pq.add(start);
        // All other nodes will implicitly have infinity distance via getOrDefault

        MapNode pathEndNode = null;
        int exploredNodes = 0;
        int settledNodesCount = 0;

        while (!pq.isEmpty()) {
            MapNode u = pq.poll();
            exploredNodes++;

            if (u.equalsByCoordinates(target)) {
                pathEndNode = u; // Target found
                // logger.debug("Target reached after exploring {} nodes, settled {} nodes", 
                //            exploredNodes, settledNodesCount);
                break;
            }

            // If already settled (shortest path found), skip.
            // Or if distance is MAX_VALUE, it means it's unreachable from processed nodes.
            if (settledNodes.contains(u) || distances.getOrDefault(u, Integer.MAX_VALUE) == Integer.MAX_VALUE) {
                continue;
            }
            settledNodes.add(u);
            settledNodesCount++;

            // Explore neighbors (Up, Down, Left, Right)
            for (Direction dir : Direction.values()) {
                MapNode v = getNodeInDirection(u, dir); // Helper to get node from GameMap

                if (v != null && !settledNodes.contains(v)) { // Neighbor exists and not yet settled
                    int costUV = getMovementCost(u, v);
                    if (costUV == Integer.MAX_VALUE) { // Impassable neighbor
                        continue;
                    }

                    int distanceU = distances.getOrDefault(u, Integer.MAX_VALUE);
                    int newDistToV = distanceU + costUV;

                    if (newDistToV < distances.getOrDefault(v, Integer.MAX_VALUE)) {
                        distances.put(v, newDistToV);
                        predecessors.put(v, u);
                        
                        // logger.trace("Updated distance to {} from {} to {} (via {})", 
                        //            v.printCoordinates(), 
                        //            distances.getOrDefault(v, Integer.MAX_VALUE), 
                        //            newDistToV, u.printCoordinates());
                        
                        // Remove and re-add to update priority in PQ if it was already there
                        // (or rely on PQ handling updates if it supports decrease-key,
                        // standard Java PQ doesn't directly, so remove/add is a common workaround)
                        pq.remove(v); // Remove if it exists
                        pq.add(v);    // Add with updated distance for correct prioritization
                    }
                }
            }
        }

        if (pathEndNode == null) {
            // logger.warn("No path found from {} to {} after exploring {} nodes", 
            //            start.printCoordinates(), target.printCoordinates(), exploredNodes);
            System.out.println("WayFinder.findShortestPath (Dijkstra): No path found from (" + start.getX() + "," + start.getY() +
                               ") to (" + target.getX() + "," + target.getY() + ").");
            return path; // Return empty path if target not reached
        }

        // Reconstruct the path by backtracking from the target using predecessors
        MapNode currentTrace = pathEndNode;
        int pathLength = 0;
        while (currentTrace != null) {
            path.add(currentTrace);
            currentTrace = predecessors.get(currentTrace);
            pathLength++;
        }
        Collections.reverse(path); // Reverse to get path from start to target

        int totalCost = distances.get(pathEndNode);
        // logger.info("Shortest path found: {} nodes, total cost: {}, explored {} nodes", 
        //            pathLength, totalCost, exploredNodes);
        // logger.trace("Path: {}", path.stream()
        //         .map(MapNode::printCoordinates)
        //         .collect(java.util.stream.Collectors.joining(" -> ")));

        return path;
    }
    /**
     * Calculates the movement cost between two adjacent MapNodes based on their terrain types.
     * @param from The starting MapNode.
     * @param to The target MapNode.
     * @return The movement cost as an integer.
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

        // Fallback for unhandled transitions (e.g., if 'from' was somehow Water)
        // This should ideally not be reached in normal operation if 'from' is always a valid standing point.
        System.err.println("WayFinder.getMovementCost: Unhandled terrain transition from " + fromTerrain + " to " + toTerrain);
        return Integer.MAX_VALUE;
    }

    /**
     * Get node in a specific direction from current node
     * @param startNode The starting MapNode.
     * @param direction The Direction to move in.
     * @return The MapNode in the specified direction, or null if out of bounds.
     */
    public MapNode getNodeInDirection(MapNode startNode, Direction direction) {
        if (startNode == null || gameState == null || gameState.getMap() == null) {
            return null;
        }

        int x = startNode.getX();
        int y = startNode.getY();

        switch (direction) {
            case UP: y--; break;
            case DOWN: y++; break;
            case LEFT: x--; break;
            case RIGHT: x++; break;
        }

        try {
            return gameState.getMap().getNode(x, y);
        } catch (IllegalArgumentException e) {
            // Out of bounds
            return null;
        }
    }

    /**
     * Gets the direction to a neighbor node from the current position.
     * @param neighbor The neighbor MapNode to find the direction to.
     * @return The Direction to the neighbor node, or null if the neighbor is not adjacent.
     */
    public Direction getDirectionToNeighbor(MapNode neighbor){
        if (neighbor == null){
            System.err.println("WayFinder: Neighbor node is null, cannot determine direction.");
            throw new IllegalArgumentException("Neighbor node cannot be null.");
        }
        int currentX = gameState.getCurrentPlayerState().getCurrentPosition().getX();
        int currentY = gameState.getCurrentPlayerState().getCurrentPosition().getY();
        int neighborX = neighbor.getX();
        int neighborY = neighbor.getY();
        // Only allow direct neighbors (one step in x or y, not both, and not diagonal)
        if ((Math.abs(currentX - neighborX) + Math.abs(currentY - neighborY)) != 1) {
            System.err.println("Neighbor: "+ neighbor.toString());
            new Throwable("Neighbor node is not a valid neighbor of the current position.").printStackTrace(System.err);
            return null;
        }
        if (neighborX > currentX) {
            return Direction.RIGHT;
        } else if (neighborX < currentX) {
            return Direction.LEFT;
        } else if (neighborY > currentY) {
            return Direction.DOWN;
        } else if (neighborY < currentY) {
            return Direction.UP;
        }
        return null;
    }
    /**
     * Calculates the total cost to reach a target node from a starting node.
     * This method uses the findShortestPath method to get the path and then sums the movement costs.
     * It returns -1 if the path is not found or if the start or target nodes are null.
     * @param start The starting MapNode.
     * @param target The target MapNode.
     * @return The total movement cost as an integer, or -1 if the path is not found.
     */
    public int getCostToReachNode(MapNode start, MapNode target) {
        // logger.debug("Calculating cost to reach {} from {}", 
        //             target != null ? target.printCoordinates() : "null",
        //             start != null ? start.printCoordinates() : "null");
        
        if (start == null || gameState == null || gameState.getMap() == null) {
            // logger.error("Invalid parameters for cost calculation - start: {}, gameState: {}, map: {}", 
            //             start != null, gameState != null, 
            //             gameState != null ? gameState.getMap() != null : false);
            System.err.println("WayFinder.getCostToReachNode: Node, gameState, or map is null.");
            return -1;
        }

        MapNode currentPosition = gameState.getCurrentPlayerState().getCurrentPosition();
        if (currentPosition == null) {
            // logger.error("Current position is null, cannot calculate cost");
            System.err.println("WayFinder.getCostToReachNode: Current position is null.");
            return -1;
        }

        if (target == null) {
            // logger.error("Target node is null, cannot calculate cost");
            System.err.println("WayFinder.getCostToReachNode: Target node is null.");
            return -1;
        }

        ArrayList<MapNode> path = findShortestPath(start, target);
        if (path.isEmpty()) {
            // logger.warn("No path found from {} to {}, returning -1", 
            //            start.printCoordinates(), target.printCoordinates());
            return -1;
        }

        int totalCost = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            int segmentCost = getMovementCost(path.get(i), path.get(i + 1));
            totalCost += segmentCost;
            // logger.trace("Segment {} -> {}: cost {}", 
            //             path.get(i).printCoordinates(), 
            //             path.get(i + 1).printCoordinates(), 
            //             segmentCost);
        }
        
        // logger.debug("Total cost from {} to {}: {}", 
        //             start.printCoordinates(), target.printCoordinates(), totalCost);
        return totalCost;
    }

    @Override
    public void update(GameState gameState) {
        // logger.trace("ShortestPathFinder received GameState update");
        this.gameState = gameState;
        //System.out.print(StaticColors.BLUE + "S" + StaticColors.RESET);
    }
    // for testing purposes, TDD
    public Object getGameState() {
       return gameState;
    }

}
