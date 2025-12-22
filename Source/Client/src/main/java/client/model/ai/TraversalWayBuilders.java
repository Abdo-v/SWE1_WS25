package client.model.ai;

import java.util.ArrayList;
import java.util.LinkedHashMap;

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
        LinkedHashMap<MapNode, Boolean> grassTraversal = new LinkedHashMap<>();
        if (halfMap == null || halfMap.getMapNodes() == null) {
            return grassTraversal;
        }

        for (MapNode node : halfMap.getMapNodes()) {
            if (node != null && node.getTerrain() == Terrain.GRASS) {
                grassTraversal.put(node, false);
            }
        }
        return grassTraversal;
    }

    static LinkedHashMap<MapNode, Boolean> mountainFields(GameMap map) {
        LinkedHashMap<MapNode, Boolean> mountains = new LinkedHashMap<>();
        if (map == null || map.getGameMapNodes() == null) {
            return mountains;
        }

        for (MapNode node : map.getGameMapNodes()) {
            if (node != null && node.getTerrain() == Terrain.MOUNTAIN) {
                mountains.put(node, false);
            }
        }
        return mountains;
    }

    static ArrayList<MapNode> toUnvisitedNodes(LinkedHashMap<MapNode, Boolean> visited) {
        ArrayList<MapNode> nodes = new ArrayList<>();
        if (visited == null) {
            return nodes;
        }
        for (MapNode node : visited.keySet()) {
            if (Boolean.TRUE.equals(visited.get(node))) {
                nodes.add(node);
            }
        }
        return nodes;
    }
}
