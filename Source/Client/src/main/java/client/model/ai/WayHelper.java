package client.model.ai;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import client.model.GameState;
import client.model.PlayerState;
import client.model.mapper.MapNode;
import client.model.mapper.PlayerHalfMap;

/**
 * Helper class for managing traversal paths and half-map arrangements.
 * This class encapsulates logic for organizing and tracking map nodes for traversal strategies.
 */
public class WayHelper implements client.observer.util.Observer {

    private Optional<GameState> gameState;
    private LinkedHashMap<MapNode, Boolean> halfMapVisitedGrassFields;
    private LinkedHashMap<MapNode, Boolean> oppHalfMapVisitedGrassFields;
    private LinkedHashMap<MapNode, Boolean> allMountainFields;

    /**
     * Default constructor for WayHelper.
     */
    public WayHelper() {
        this.gameState = Optional.empty();
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
        return gameState
            .flatMap(state -> state.getMap().map(map -> {
                Optional<MapNode> start = state.getCurrentPlayerState()
                        .flatMap(PlayerState::getCurrentPosition)
                        .or(state::getOwnFortPosition);

                return start
                        .map(pos -> {
                            PlayerHalfMap ownHalfMap = HalfMapSnakeArranger.arrangeOwnHalf(map, pos);
                            return toMapNodeBooleanMap(TraversalWayBuilders.grassTraversal(ownHalfMap));
                        })
                        .orElseGet(LinkedHashMap::new);
            }))
            .orElseGet(LinkedHashMap::new);
    }

    /**
     * Returns a LinkedHashMap of grass nodes in the opponent's half-map, initialized as unvisited.
     * The keys are MapNode objects representing grass nodes, and the values are Booleans indicating if the node has been visited.
     * @return A LinkedHashMap of grass nodes with their visited status.
     */
    public LinkedHashMap<MapNode, Boolean> getTraverseWayForOpponentHalf(){
        return gameState
            .flatMap(state -> state.getMap().map(map -> state.getOwnFortPosition()
                    .map(start -> {
                        PlayerHalfMap opponentHalfMap = HalfMapSnakeArranger.arrangeOpponentHalf(map, start);
                        return toMapNodeBooleanMap(TraversalWayBuilders.grassTraversal(opponentHalfMap));
                    })
                    .orElseGet(LinkedHashMap::new)))
            .orElseGet(LinkedHashMap::new);
    }

    /**
     * Returns a LinkedHashMap of grass nodes in the opponent's half-map from a specific position, initialized as unvisited.
     * @param currentPosition The current position from which to arrange the opponent half-map.
     * @return A LinkedHashMap of grass nodes with their visited status.
     */
    private LinkedHashMap<MapNode, Boolean> getTraverseWayForOpponentHalf(MapNode currentPosition){
        Objects.requireNonNull(currentPosition, "currentPosition must not be null");
        return gameState
                .flatMap(GameState::getMap)
                .map(map -> HalfMapSnakeArranger.arrangeOpponentHalf(map, currentPosition))
            .map(TraversalWayBuilders::grassTraversal)
                .map(WayHelper::toMapNodeBooleanMap)
                .orElseGet(LinkedHashMap::new);
    }

    /**
     * Filters the traversal way based on enemy position, keeping only nodes reachable within 8 moves.
     * @param enemyTruePosition The enemy's true position.
     * @return A filtered LinkedHashMap containing only reachable nodes.
     */
    public LinkedHashMap<MapNode, Boolean> getFilteredTraverseWay(MapNode enemyTruePosition){
        Objects.requireNonNull(enemyTruePosition, "enemyTruePosition must not be null");
        GameState state = gameState.orElseThrow(() -> new IllegalStateException("GameState must be initialized"));
        MapNode currentPosition = state.getCurrentPlayerState()
                .flatMap(PlayerState::getCurrentPosition)
                .orElseThrow(() -> new IllegalStateException("Current player position must be initialized"));

        var map = state.getMap().orElseThrow(() -> new IllegalStateException("GameMap must be initialized"));
        LinkedHashMap<MapNode, Boolean> fullTraversalWay = getTraverseWayForOpponentHalf(currentPosition);
        LinkedHashMap<MapNode, Boolean> filteredTraversalWay = new LinkedHashMap<>();
        
        for (MapNode node : fullTraversalWay.keySet()) {
            int cost = GridDijkstra.shortestPathCost(map, enemyTruePosition, node, MovementCostProfile.WAY_HELPER);
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
        return gameState
                .flatMap(GameState::getMap)
            .map(TraversalWayBuilders::mountainFields)
                .map(WayHelper::toMapNodeBooleanMap)
                .orElseGet(LinkedHashMap::new);
    }

    private static LinkedHashMap<MapNode, Boolean> toMapNodeBooleanMap(Map<?, ?> raw) {
        LinkedHashMap<MapNode, Boolean> typed = new LinkedHashMap<>();
        if (raw.isEmpty()) {
            return typed;
        }

        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key instanceof MapNode node && value instanceof Boolean visited) {
                typed.put(node, visited);
            }
        }
        return typed;
    }

    /**
     * Returns the arranged own half map based on the current position of the player.
     * The arrangement is done in a Y-snake traversal pattern, starting from the player's current position.
     * 
     * @param currentPosition The current position of the player.
     * @return The arranged PlayerHalfMap containing nodes in Y-snake order.
     */
    public PlayerHalfMap getArrangedOwnHalfMap(MapNode currentPosition){
        Objects.requireNonNull(currentPosition, "currentPosition must not be null");
        GameState state = gameState.orElseThrow(() -> new IllegalStateException("GameState must be initialized"));
        return state.getMap()
            .map(map -> HalfMapSnakeArranger.arrangeOwnHalf(map, currentPosition))
            .orElseThrow(() -> new IllegalStateException("GameMap must be initialized"));
    }

    @Override
    public void update(GameState gameState) {
        this.gameState = Optional.of(Objects.requireNonNull(gameState, "gameState must not be null"));
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
        this.halfMapVisitedGrassFields = Objects.requireNonNull(halfMapVisitedGrassFields, "halfMapVisitedGrassFields must not be null");
    }

    public void setOppHalfMapVisitedGrassFields(LinkedHashMap<MapNode, Boolean> oppHalfMapVisitedGrassFields) {
        this.oppHalfMapVisitedGrassFields = Objects.requireNonNull(oppHalfMapVisitedGrassFields, "oppHalfMapVisitedGrassFields must not be null");
    }

    public void setAllMountainFields(LinkedHashMap<MapNode, Boolean> allMountainFields) {
        this.allMountainFields = Objects.requireNonNull(allMountainFields, "allMountainFields must not be null");
    }

}
