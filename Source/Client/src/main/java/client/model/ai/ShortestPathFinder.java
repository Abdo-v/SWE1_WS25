package client.model.ai;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import client.model.Direction;
import client.model.GameState;
import client.model.common.DebugSettings;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

class ShortestPathFinder implements client.observer.util.Observer {

    // private static final Logger logger = LoggerFactory.getLogger(ShortestPathFinder.class);
    private Optional<GameState> gameState;

    public ShortestPathFinder() {
        this.gameState = Optional.empty();
    }

    /**
     * Finds the next valid node to move towards the target.
    * @return The Direction to move towards the target node.
     */
    public Optional<Direction> findNextValidNodeToTarget(MapNode targetMapNode){
        Objects.requireNonNull(targetMapNode, "targetMapNode is required");

        GameState state = gameState.orElseThrow(() -> new IllegalStateException("GameState must be set before pathfinding"));
        MapNode target = targetMapNode;
        MapNode current = state.getCurrentPlayerState()
            .flatMap(player -> player.getCurrentPosition())
            .orElseThrow(() -> new IllegalStateException("Current position must be set before finding next valid direction."));
        
        // logger.trace("Pathfinding from {} to {}", current.printCoordinates(), target.printCoordinates());
        ArrayList<MapNode> path = findShortestPath(current, target);

        if (path.isEmpty()) {
            // logger.warn("No valid path found to target {}", target.printCoordinates());
            if (DebugSettings.isDebugEnabled()) {
                System.err.println("WayFinder: No valid path found to target node (empty path).");
            }
            return Optional.empty();
        }

        // Path includes start at index 0; the next step is index 1.
        if (path.size() < 2) {
            if (DebugSettings.isDebugEnabled()) {
                System.err.println("WayFinder: Current node already equals target (no next step).");
            }
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
     *         Returns an empty list if no path is found, or if start/target is missing or invalid.
     *         If start and target are the same, returns a list containing just the start node.
     */
    private ArrayList<MapNode> findShortestPath(MapNode start, MapNode target) {
        Objects.requireNonNull(start, "start is required");
        Objects.requireNonNull(target, "target is required");

        ArrayList<MapNode> path = new ArrayList<>();

        if (gameState.isEmpty() || gameState.flatMap(GameState::getMap).isEmpty()) {
            if (DebugSettings.isDebugEnabled()) {
                System.err.println("WayFinder.findShortestPath (Dijkstra): gameState or map is missing.");
            }
            return path;
        }

        MapNode safeStart = start;
        MapNode safeTarget = target;

        if (safeStart.equalsByCoordinates(safeTarget)) {
            // logger.debug("Start and target are the same, returning single-node path");
            path.add(safeStart);
            return path;
        }

        GameState state = gameState.orElseThrow();
        var map = state.getMap().orElseThrow(() -> new IllegalStateException("GameMap must be set before pathfinding"));
        ArrayList<MapNode> computed = GridDijkstra.shortestPath(map, safeStart, safeTarget, MovementCostProfile.SHORTEST_PATH);
        if (computed.isEmpty()) {
            if (DebugSettings.isDebugEnabled()) {
                System.err.println("WayFinder.findShortestPath (Dijkstra): No path found from (" + safeStart.getX() + "," + safeStart.getY() +
                                   ") to (" + safeTarget.getX() + "," + safeTarget.getY() + ").");
            }
        }
        return computed;
    }

    /**
     * Get node in a specific direction from current node
     * @param startNode The starting MapNode.
     * @param direction The Direction to move in.
      * @return The MapNode in the specified direction.
     */
    public MapNode getNodeInDirection(MapNode startNode, Direction direction) {
          Objects.requireNonNull(startNode, "startNode is required");
          Objects.requireNonNull(direction, "direction is required");

        GameState state = gameState.orElseThrow(() -> new IllegalStateException("GameState must be set before navigation"));
        var map = state.getMap().orElseThrow(() -> new IllegalStateException("GameMap must be set before navigation"));
        return GridNavigation.getNodeInDirection(map, startNode, direction).orElseThrow(
                () -> new IllegalArgumentException("Requested direction leads out of bounds")
        );
    }

    /**
     * Gets the direction to a neighbor node from the current position.
     * @param neighbor The neighbor MapNode to find the direction to.
      * @return The Direction to the neighbor node, if the neighbor is adjacent.
     */
    public Optional<Direction> getDirectionToNeighbor(MapNode neighbor){
          Objects.requireNonNull(neighbor, "neighbor is required");
        GameState state = gameState.orElseThrow(() -> new IllegalStateException("GameState must be set before navigation"));
        MapNode current = state.getCurrentPlayerState()
                .flatMap(player -> player.getCurrentPosition())
                .orElseThrow(() -> new IllegalStateException("Current position must be initialized"));
        return GridNavigation.getDirectionToNeighbor(current, neighbor);
    }

    /**
     * Computes a full Dijkstra cost-map from the current player position.
     *
     * <p>Use this when you want to compare many candidate targets in the same turn without
     * rerunning pathfinding for each candidate.</p>
     *
     * @param costProfile Movement cost profile (e.g. {@link MovementCostProfile#SHORTEST_PATH}).
     * @return Map of reachable nodes to their minimum action cost. Unreachable nodes are absent.
     */
    public Map<MapNode, Integer> computeCostMapFromCurrent(MovementCostProfile costProfile) {
        Objects.requireNonNull(costProfile, "costProfile is required");

        GameState state = gameState.orElseThrow(() -> new IllegalStateException("GameState must be set before cost-map computation"));
        MapNode current = state.getCurrentPlayerState()
                .flatMap(player -> player.getCurrentPosition())
                .orElseThrow(() -> new IllegalStateException("Current position must be set before cost-map computation"));
        var map = state.getMap().orElseThrow(() -> new IllegalStateException("GameMap must be set before cost-map computation"));
        return GridDijkstra.shortestPathCosts(map, current, costProfile);
    }

    @Override
    public void update(GameState gameState) {
        // logger.trace("ShortestPathFinder received GameState update");
                    this.gameState = Optional.of(Objects.requireNonNull(gameState, "gameState is required"));
        //System.out.print(StaticColors.BLUE + "S" + StaticColors.RESET);
    }

}
