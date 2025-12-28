package client.model.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Optional;
import java.util.Objects;

import client.model.mapper.GameMap;
import client.model.mapper.MapNode;

/**
 * Internal Dijkstra implementation for the game grid.
 *
 * Package-private on purpose: extracted to keep the larger AI classes focused.
 */
final class GridDijkstra {

    private GridDijkstra() {
        // utility
    }

    private static final int[][] DIRECTIONS = { {0, -1}, {0, 1}, {-1, 0}, {1, 0} };
    private static final String MAP_REQUIRED = MessageConfig.MAP_REQUIRED;
    private static final String START_REQUIRED = MessageConfig.START_REQUIRED;
    private static final String TARGET_REQUIRED = MessageConfig.TARGET_REQUIRED;
    private static final String COST_PROFILE_REQUIRED = MessageConfig.COST_PROFILE_REQUIRED;

    private static Optional<MapNode> getNodeSafely(GameMap map, int x, int y) {
        try {
            return Optional.of(Objects.requireNonNull(map.getNode(x, y)));
        } catch (NullPointerException e) {
            return Optional.empty();
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    static int shortestPathCost(GameMap map, MapNode start, MapNode target, MovementCostProfile costProfile) {
        Objects.requireNonNull(map, MAP_REQUIRED);
        Objects.requireNonNull(start, START_REQUIRED);
        Objects.requireNonNull(target, TARGET_REQUIRED);
        Objects.requireNonNull(costProfile, COST_PROFILE_REQUIRED);
        if (start.equalsByCoordinates(target)) {
            return 0;
        }

        SearchState stateOne = createSearchState(start);
        Map<MapNode, Integer> distances = stateOne.distances();
        Set<MapNode> settledNodes = stateOne.settledNodes();
        PriorityQueue<MapNode> pq = stateOne.priorityQueue();

        while (!pq.isEmpty()) {
            MapNode u = pq.poll();

            if (u.equalsByCoordinates(target)) {
                return distances.getOrDefault(u, Integer.MAX_VALUE);
            }

            if (settledNodes.contains(u) || distances.getOrDefault(u, Integer.MAX_VALUE) == Integer.MAX_VALUE) {
                continue;
            }
            settledNodes.add(u);

            relaxNeighbors(map, u, settledNodes, distances, pq, costProfile, Optional.empty());
        }

        return Integer.MAX_VALUE;
    }

    static ArrayList<MapNode> shortestPath(GameMap map, MapNode start, MapNode target, MovementCostProfile costProfile) {
        ArrayList<MapNode> path = new ArrayList<>();
        Objects.requireNonNull(map, MAP_REQUIRED);
        Objects.requireNonNull(start, START_REQUIRED);
        Objects.requireNonNull(target, TARGET_REQUIRED);
        Objects.requireNonNull(costProfile, COST_PROFILE_REQUIRED);

        if (start.equalsByCoordinates(target)) {
            path.add(start);
            return path;
        }

        SearchState stateTwo = createSearchState(start);
        Map<MapNode, Integer> distances = stateTwo.distances();
        Map<MapNode, MapNode> predecessors = new HashMap<>();
        Set<MapNode> settledNodes = stateTwo.settledNodes();
        PriorityQueue<MapNode> pq = stateTwo.priorityQueue();

        Optional<MapNode> pathEndNode = Optional.empty();

        while (!pq.isEmpty()) {
            MapNode u = pq.poll();

            if (u.equalsByCoordinates(target)) {
                pathEndNode = Optional.of(u);
                break;
            }

            if (settledNodes.contains(u) || distances.getOrDefault(u, Integer.MAX_VALUE) == Integer.MAX_VALUE) {
                continue;
            }
            settledNodes.add(u);

            relaxNeighbors(map, u, settledNodes, distances, pq, costProfile, Optional.of(predecessors));
        }

        if (pathEndNode.isEmpty()) {
            return path;
        }

        Optional<MapNode> currentTrace = pathEndNode;
        while (currentTrace.isPresent()) {
            MapNode node = currentTrace.orElseThrow();
            path.add(node);
            try {
                currentTrace = Optional.of(Objects.requireNonNull(predecessors.get(node)));
            } catch (NullPointerException e) {
                currentTrace = Optional.empty();
            }
        }
        java.util.Collections.reverse(path);
        return path;
    }

    /**
     * Computes the shortest-path cost from {@code start} to every reachable node.
     *
     * <p>This is useful when many candidate targets need to be compared in the same turn:
     * run Dijkstra once and then look up costs in O(1) per candidate.</p>
     *
     * @param map The game map.
     * @param start The starting node.
     * @param costProfile The movement cost profile.
     * @return A map of nodes to their minimum action-cost from {@code start}. Unreachable nodes are absent.
     */
    static Map<MapNode, Integer> shortestPathCosts(GameMap map, MapNode start, MovementCostProfile costProfile) {
        Objects.requireNonNull(map, MAP_REQUIRED);
        Objects.requireNonNull(start, START_REQUIRED);
        Objects.requireNonNull(costProfile, COST_PROFILE_REQUIRED);

        SearchState state = createSearchState(start);
        Map<MapNode, Integer> distances = state.distances();
        Set<MapNode> settledNodes = state.settledNodes();
        PriorityQueue<MapNode> pq = state.priorityQueue();

        while (!pq.isEmpty()) {
            MapNode u = pq.poll();

            if (settledNodes.contains(u) || distances.getOrDefault(u, Integer.MAX_VALUE) == Integer.MAX_VALUE) {
                continue;
            }
            settledNodes.add(u);

            relaxNeighbors(map, u, settledNodes, distances, pq, costProfile, Optional.empty());
        }

        return distances;
    }

    private static void relaxNeighbors(
            GameMap map,
            MapNode current,
            Set<MapNode> settledNodes,
            Map<MapNode, Integer> distances,
            PriorityQueue<MapNode> pq,
            MovementCostProfile costProfile,
            Optional<Map<MapNode, MapNode>> predecessors) {
        for (int[] dir : DIRECTIONS) {
            int newX = current.getX() + dir[0];
            int newY = current.getY() + dir[1];

            getNodeSafely(map, newX, newY).ifPresent(neighbor -> {
                if (settledNodes.contains(neighbor)) {
                    return;
                }

                int edgeCost = costProfile.cost(current, neighbor);
                if (edgeCost == Integer.MAX_VALUE) {
                    return;
                }

                int currentDistance = distances.getOrDefault(current, Integer.MAX_VALUE);
                int updatedDistance = currentDistance + edgeCost;
                int previousDistance = distances.getOrDefault(neighbor, Integer.MAX_VALUE);

                if (updatedDistance < previousDistance) {
                    distances.put(neighbor, updatedDistance);
                    predecessors.ifPresent(p -> p.put(neighbor, current));
                    pq.remove(neighbor);
                    pq.add(neighbor);
                }
            });
        }
    }

    private static SearchState createSearchState(MapNode start) {
        Map<MapNode, Integer> distances = new HashMap<>();
        Comparator<MapNode> nodeComparator = Comparator.comparingInt(node -> distances.getOrDefault(node, Integer.MAX_VALUE));
        PriorityQueue<MapNode> priorityQueue = new PriorityQueue<>(nodeComparator);
        distances.put(start, 0);
        priorityQueue.add(start);
        return new SearchState(distances, new HashSet<>(), priorityQueue);
    }

    private record SearchState(
            Map<MapNode, Integer> distances,
            Set<MapNode> settledNodes,
            PriorityQueue<MapNode> priorityQueue) {}
}
