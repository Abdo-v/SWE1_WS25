package client.model.ai;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import client.model.Direction;
import client.model.GameState;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

public class ShortestPathFinder implements client.observer.util.Observer {

    // private static final Logger logger = LoggerFactory.getLogger(ShortestPathFinder.class);
    private Optional<GameState> gameState;

    public ShortestPathFinder(GameState gameState) {
        this.gameState = Optional.of(Objects.requireNonNull(gameState, "gameState must not be null"));
    }

    public ShortestPathFinder() {
        this.gameState = Optional.empty();
    }

    /**
     * Finds the next valid node to move towards the target.
    * @return The Direction to move towards the target node.
     */
    public Optional<Direction> findNextValidNodeToTarget(MapNode targetMapNode){
        Objects.requireNonNull(targetMapNode, "targetMapNode must not be null");

        GameState state = gameState.orElseThrow(() -> new IllegalStateException("GameState must be set before pathfinding"));
        MapNode target = targetMapNode;
        MapNode current = state.getCurrentPlayerState()
            .flatMap(player -> player.getCurrentPosition())
            .orElseThrow(() -> new IllegalStateException("Current position must be set before finding next valid direction."));
        
        // logger.trace("Pathfinding from {} to {}", current.printCoordinates(), target.printCoordinates());
        ArrayList<MapNode> path = findShortestPath(current, target);

        if (path.isEmpty()) {
            // logger.warn("No valid path found to target {}", target.printCoordinates());
            System.out.println("WayFinder: No valid path found to target node: path is empty.");
            return Optional.empty();
        }

        // Path includes start at index 0; the next step is index 1.
        if (path.size() < 2) {
            System.out.println("WayFinder: Current node is the last in the path, no next node to move towards.");
            return Optional.empty();
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
        ArrayList<MapNode> path = new ArrayList<>();
        Optional<MapNode> startNode = Optional.ofNullable(start);
        Optional<MapNode> targetNode = Optional.ofNullable(target);
        Optional<GameState> stateOptional = gameState;
        Optional<GameMap> mapOptional = gameState.flatMap(GameState::getMap);

        if (startNode.isEmpty() || targetNode.isEmpty() || stateOptional.isEmpty() || mapOptional.isEmpty()) {
            System.err.println("WayFinder.findShortestPath (Dijkstra): Start, target, gameState, or map is missing.");
            return path;
        }

        MapNode safeStart = startNode.orElseThrow();
        MapNode safeTarget = targetNode.orElseThrow();

        if (safeStart.equalsByCoordinates(safeTarget)) {
            // logger.debug("Start and target are the same, returning single-node path");
            path.add(safeStart);
            return path;
        }

        GameState state = gameState.orElseThrow();
        var map = state.getMap().orElseThrow(() -> new IllegalStateException("GameMap must be set before pathfinding"));
        ArrayList<MapNode> computed = GridDijkstra.shortestPath(map, safeStart, safeTarget, MovementCostProfile.SHORTEST_PATH);
        if (computed.isEmpty()) {
            System.out.println("WayFinder.findShortestPath (Dijkstra): No path found from (" + safeStart.getX() + "," + safeStart.getY() +
                               ") to (" + safeTarget.getX() + "," + safeTarget.getY() + ").");
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
        Objects.requireNonNull(startNode, "startNode must not be null");
        Objects.requireNonNull(direction, "direction must not be null");

        GameState state = gameState.orElseThrow(() -> new IllegalStateException("GameState must be set before navigation"));
        var map = state.getMap().orElseThrow(() -> new IllegalStateException("GameMap must be set before navigation"));
        return GridNavigation.getNodeInDirection(map, startNode, direction).orElseThrow(
                () -> new IllegalArgumentException("Requested direction leads out of bounds")
        );
    }

    /**
     * Gets the direction to a neighbor node from the current position.
     * @param neighbor The neighbor MapNode to find the direction to.
     * @return The Direction to the neighbor node, or null if the neighbor is not adjacent.
     */
    public Optional<Direction> getDirectionToNeighbor(MapNode neighbor){
        Objects.requireNonNull(neighbor, "neighbor must not be null");
        GameState state = gameState.orElseThrow(() -> new IllegalStateException("GameState must be set before navigation"));
        MapNode current = state.getCurrentPlayerState()
                .flatMap(player -> player.getCurrentPosition())
                .orElseThrow(() -> new IllegalStateException("Current position must be initialized"));
        return GridNavigation.getDirectionToNeighbor(current, neighbor);
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
        Optional<MapNode> startNode = Optional.ofNullable(start);
        Optional<MapNode> targetNode = Optional.ofNullable(target);
        Optional<GameState> stateOptional = gameState;

        if (startNode.isEmpty() || targetNode.isEmpty() || stateOptional.isEmpty() || gameState.flatMap(GameState::getMap).isEmpty()) {
            System.err.println("WayFinder.getCostToReachNode: Node, gameState, or map is missing.");
            return -1;
        }

        GameState state = gameState.orElseThrow();
        MapNode currentPosition = state.getCurrentPlayerState()
                .flatMap(player -> player.getCurrentPosition())
                .orElseThrow(() -> new IllegalStateException("Current position must be initialized"));

        var map = state.getMap().orElseThrow(() -> new IllegalStateException("GameMap must be set before cost calculation"));

        int cost = GridDijkstra.shortestPathCost(map, startNode.orElseThrow(), targetNode.orElseThrow(), MovementCostProfile.SHORTEST_PATH);
        return cost == Integer.MAX_VALUE ? -1 : cost;
    }

    @Override
    public void update(GameState gameState) {
        // logger.trace("ShortestPathFinder received GameState update");
          this.gameState = Optional.of(Objects.requireNonNull(gameState, "gameState must not be null"));
        //System.out.print(StaticColors.BLUE + "S" + StaticColors.RESET);
    }
    // for testing purposes, TDD
     public Optional<GameState> getGameState() {
         return gameState;
    }

}
