package client.model.ai;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;

import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.PlayerHalfMap;
import client.model.mapper.Terrain;

/**
 * Small builders/collectors for traversal data structures.
 *
 * Package-private on purpose: extracted from WayHelper for SRP.
 */
final class TraversalWayBuilders {

    private TraversalWayBuilders() {
        // utility
    }

    static LinkedHashMap<MapNode, Boolean> grassTraversal(PlayerHalfMap halfMap) {
        Objects.requireNonNull(halfMap, "halfMap is required");
        LinkedHashMap<MapNode, Boolean> grassTraversal = new LinkedHashMap<>();
        for (MapNode node : halfMap.getMapNodes()) {
            if (Objects.nonNull(node) && node.getTerrain() == Terrain.GRASS) {
                grassTraversal.put(node, false);
            }
        }
        return grassTraversal;
    }

    static LinkedHashMap<MapNode, Boolean> mountainFields(GameMap map) {
        Objects.requireNonNull(map, "map is required");
        LinkedHashMap<MapNode, Boolean> mountains = new LinkedHashMap<>();
        for (MapNode node : map.getGameMapNodes()) {
            if (Objects.nonNull(node) && node.getTerrain() == Terrain.MOUNTAIN) {
                mountains.put(node, false);
            }
        }
        return mountains;
    }

    static ArrayList<MapNode> toUnvisitedNodes(LinkedHashMap<MapNode, Boolean> visited) {
        Objects.requireNonNull(visited, "visited is required");
        ArrayList<MapNode> nodes = new ArrayList<>();
        for (MapNode node : visited.keySet()) {
            // Convention in this codebase: map value == true means "already visited".
            // Therefore, "unvisited" is anything that is not explicitly true.
            if (!Boolean.TRUE.equals(visited.get(node))) {
                nodes.add(node);
            }
        }
        return nodes;
    }
}
