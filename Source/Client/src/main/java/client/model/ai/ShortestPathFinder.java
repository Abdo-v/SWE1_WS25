package client.model.ai;

import java.util.ArrayList;

import client.model.Direction;
import client.model.GameState;
import client.model.mapper.MapNode;
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

        if (path == null || path.isEmpty()) {
            // logger.warn("No valid path found to target {}", target.printCoordinates());
            System.out.println("WayFinder: No valid path found to target node: path is empty.");
            return null; // No valid path to target
        }

        // Path includes start at index 0; the next step is index 1.
        if (path.size() < 2) {
            System.out.println("WayFinder: Current node is the last in the path, no next node to move towards.");
            return null;
        }

        MapNode nextNode = path.get(1);
        return getDirectionToNeighbor(nextNode);
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

        ArrayList<MapNode> computed = GridDijkstra.shortestPath(gameState.getMap(), start, target, MovementCostProfile.SHORTEST_PATH);
        if (computed.isEmpty()) {
            System.out.println("WayFinder.findShortestPath (Dijkstra): No path found from (" + start.getX() + "," + start.getY() +
                               ") to (" + target.getX() + "," + target.getY() + ").");
        }
        return computed;
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

        return GridNavigation.getNodeInDirection(gameState.getMap(), startNode, direction);
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
        MapNode current = gameState.getCurrentPlayerState().getCurrentPosition();
        Direction direction = GridNavigation.getDirectionToNeighbor(current, neighbor);
        if (direction == null) {
            System.err.println("Neighbor: "+ neighbor.toString());
            new Throwable("Neighbor node is not a valid neighbor of the current position.").printStackTrace(System.err);
            return null;
        }
        return direction;
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

        int cost = GridDijkstra.shortestPathCost(gameState.getMap(), start, target, MovementCostProfile.SHORTEST_PATH);
        return cost == Integer.MAX_VALUE ? -1 : cost;
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
