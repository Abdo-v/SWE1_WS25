package client.model.mapper;

import client.model.common.Notification;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

final class HalfMapReachabilityValidator {
    private HalfMapReachabilityValidator() {
    }

    static void validateReachability(
            PlayerHalfMap halfMap,
            Notification notification,
            int maxX,
            int maxY,
            double minGrassPercentage,
            double minMountainPercentage
    ) {
        List<MapNode> walkableNodes = halfMap.getMapNodes().stream()
                .filter(MapNode::isWalkable)
                .collect(Collectors.toList());

        if (walkableNodes.isEmpty()) {
            if ((minGrassPercentage + minMountainPercentage) > 0) {
                notification.addError("No walkable nodes found on the map, but map rules require grass or mountain fields.");
            }
            return;
        }

        Set<MapNode> visited = new HashSet<>();
        Queue<MapNode> queue = new LinkedList<>();

        MapNode startNode = halfMap.getFortNode()
                .filter(MapNode::isWalkable)
                .orElse(walkableNodes.get(0));

        queue.add(startNode);
        visited.add(startNode);

        int[] dX = {0, 0, 1, -1};
        int[] dY = {1, -1, 0, 0};

        while (!queue.isEmpty()) {
            MapNode current = queue.poll();
            for (int i = 0; i < 4; i++) {
                int nextX = current.getX() + dX[i];
                int nextY = current.getY() + dY[i];

                if (nextX >= 0 && nextX <= maxX && nextY >= 0 && nextY <= maxY) {
                    halfMap.getMapNode(nextX, nextY)
                            .filter(MapNode::isWalkable)
                            .filter(neighbor -> !visited.contains(neighbor))
                            .ifPresent(neighbor -> {
                                visited.add(neighbor);
                                queue.add(neighbor);
                            });
                }
            }
        }

        if (visited.size() != walkableNodes.size()) {
            notification.addError("Not all walkable fields are reachable from each other. Visited: " + visited.size()
                    + ", Total Walkable: " + walkableNodes.size());
        }
    }
}
