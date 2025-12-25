package client.model.ai;

import java.util.Objects;

import client.model.mapper.MapNode;
import client.model.mapper.Terrain;

/**
 * Marks nodes as visited in the appropriate visited-maps.
 *
 * Package-private: internal AI refactoring helper.
 */
final class NodeVisitTracker {

    private final WayHelper wayHelper;

    NodeVisitTracker(WayHelper wayHelper) {
        this.wayHelper = wayHelper;
    }

    void markVisited(MapNode node, boolean ownHalf) {
        Objects.requireNonNull(node, "node must not be null");

        if (node.getTerrain() == Terrain.GRASS) {
            if (ownHalf) {
                wayHelper.getHalfMapVisitedGrassFields().put(node, true);
            } else {
                // After the "enemy first true position" becomes known, the opponent traversal map may be
                // filtered to a smaller search space. In that case, do NOT re-introduce irrelevant nodes.
                var oppVisited = wayHelper.getOppHalfMapVisitedGrassFields();
                if (!oppVisited.isEmpty() && !oppVisited.containsKey(node)) {
                    return;
                }
                oppVisited.put(node, true);
            }
            return;
        }

        if (node.getTerrain() == Terrain.MOUNTAIN) {
            wayHelper.getAllMountainFieldsMap().put(node, true);
        }
    }
}
