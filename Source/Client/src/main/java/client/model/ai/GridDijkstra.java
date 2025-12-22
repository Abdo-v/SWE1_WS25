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

    private static Optional<MapNode> getNodeSafely(GameMap map, int x, int y) {
        try {
            return Optional.ofNullable(map.getNode(x, y));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    static int shortestPathCost(GameMap map, MapNode start, MapNode target, MovementCostProfile costProfile) {
        if (Objects.isNull(map) || Objects.isNull(start) || Objects.isNull(target) || Objects.isNull(costProfile)) {
            return Integer.MAX_VALUE;
        }
        if (start.equalsByCoordinates(target)) {
            return 0;
        }

        Map<MapNode, Integer> distances = new HashMap<>();
        Set<MapNode> settledNodes = new HashSet<>();

        Comparator<MapNode> nodeComparator = Comparator.comparingInt(node -> distances.getOrDefault(node, Integer.MAX_VALUE));
        PriorityQueue<MapNode> pq = new PriorityQueue<>(nodeComparator);

        distances.put(start, 0);
        pq.add(start);

        while (!pq.isEmpty()) {
            MapNode u = pq.poll();

            if (u.equalsByCoordinates(target)) {
                return distances.getOrDefault(u, Integer.MAX_VALUE);
            }

            if (settledNodes.contains(u) || distances.getOrDefault(u, Integer.MAX_VALUE) == Integer.MAX_VALUE) {
                continue;
            }
            settledNodes.add(u);

            int currentX = u.getX();
            int currentY = u.getY();
            int[][] directions = { {0, -1}, {0, 1}, {-1, 0}, {1, 0} };

            for (int[] dir : directions) {
                int newX = currentX + dir[0];
                int newY = currentY + dir[1];

                getNodeSafely(map, newX, newY).ifPresent(neighbor -> {
                    if (settledNodes.contains(neighbor)) {
                        return;
                    }

                    int edgeCost = costProfile.cost(u, neighbor);
                    if (edgeCost == Integer.MAX_VALUE) {
                        return;
                    }

                    int currentDistanceU = distances.getOrDefault(u, Integer.MAX_VALUE);
                    int newDist = currentDistanceU + edgeCost;
                    int oldDist = distances.getOrDefault(neighbor, Integer.MAX_VALUE);

                    if (newDist < oldDist) {
                        distances.put(neighbor, newDist);
                        pq.remove(neighbor);
                        pq.add(neighbor);
                    }
                });
            }
        }

        return Integer.MAX_VALUE;
    }

    static ArrayList<MapNode> shortestPath(GameMap map, MapNode start, MapNode target, MovementCostProfile costProfile) {
        ArrayList<MapNode> path = new ArrayList<>();
        if (Objects.isNull(map) || Objects.isNull(start) || Objects.isNull(target) || Objects.isNull(costProfile)) {
            return path;
        }

        if (start.equalsByCoordinates(target)) {
            path.add(start);
            return path;
        }

        Map<MapNode, Integer> distances = new HashMap<>();
        Map<MapNode, MapNode> predecessors = new HashMap<>();
        Set<MapNode> settledNodes = new HashSet<>();

        Comparator<MapNode> nodeComparator = Comparator.comparingInt(node -> distances.getOrDefault(node, Integer.MAX_VALUE));
        PriorityQueue<MapNode> pq = new PriorityQueue<>(nodeComparator);

        distances.put(start, 0);
        pq.add(start);

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

            int currentX = u.getX();
            int currentY = u.getY();
            int[][] directions = { {0, -1}, {0, 1}, {-1, 0}, {1, 0} };

            for (int[] dir : directions) {
                int newX = currentX + dir[0];
                int newY = currentY + dir[1];

                getNodeSafely(map, newX, newY).ifPresent(v -> {
                    if (settledNodes.contains(v)) {
                        return;
                    }

                    int costUV = costProfile.cost(u, v);
                    if (costUV == Integer.MAX_VALUE) {
                        return;
                    }

                    int distanceU = distances.getOrDefault(u, Integer.MAX_VALUE);
                    int newDistToV = distanceU + costUV;

                    if (newDistToV < distances.getOrDefault(v, Integer.MAX_VALUE)) {
                        distances.put(v, newDistToV);
                        predecessors.put(v, u);
                        pq.remove(v);
                        pq.add(v);
                    }
                });
            }
        }

        if (pathEndNode.isEmpty()) {
            return path;
        }

        Optional<MapNode> currentTrace = pathEndNode;
        while (currentTrace.isPresent()) {
            MapNode node = currentTrace.orElseThrow();
            path.add(node);
            currentTrace = Optional.ofNullable(predecessors.get(node));
        }
        java.util.Collections.reverse(path);
        return path;
    }
}
