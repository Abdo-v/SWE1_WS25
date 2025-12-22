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
                wayHelper.getOppHalfMapVisitedGrassFields().put(node, true);
            }
            return;
        }

        if (node.getTerrain() == Terrain.MOUNTAIN) {
            wayHelper.getAllMountainFieldsMap().put(node, true);
        }
    }
}
