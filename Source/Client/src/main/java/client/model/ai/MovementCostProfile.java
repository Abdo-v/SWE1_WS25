package client.model.ai;

import client.model.mapper.MapNode;
import client.model.mapper.Terrain;
import java.util.Objects;

/**
 * Defines movement cost rules for different AI use-cases.
 *
 * Package-private on purpose: this is an internal refactoring detail.
 */
enum MovementCostProfile {
    WAY_HELPER {
        @Override
        int cost(MapNode from, MapNode to) {
            if (Objects.isNull(to) || to.getTerrain() == Terrain.WATER) {
                return Integer.MAX_VALUE;
            }

            Terrain fromTerrain = from.getTerrain();
            Terrain toTerrain = to.getTerrain();

            if (fromTerrain == Terrain.GRASS && toTerrain == Terrain.GRASS) return 1;
            if (fromTerrain == Terrain.GRASS && toTerrain == Terrain.MOUNTAIN) return 2;
            if (fromTerrain == Terrain.MOUNTAIN && toTerrain == Terrain.GRASS) return 1;
            if (fromTerrain == Terrain.MOUNTAIN && toTerrain == Terrain.MOUNTAIN) return 2;

            System.err.println("MovementCostProfile.WAY_HELPER: Unhandled terrain transition from " + fromTerrain + " to " + toTerrain);
            return Integer.MAX_VALUE;
        }
    },
    SHORTEST_PATH {
        @Override
        int cost(MapNode from, MapNode to) {
            if (Objects.isNull(to) || to.getTerrain() == Terrain.WATER) {
                return Integer.MAX_VALUE;
            }

            Terrain fromTerrain = from.getTerrain();
            Terrain toTerrain = to.getTerrain();

            // Keep the original ShortestPathFinder weights exactly as-is.
            if (fromTerrain == Terrain.GRASS && toTerrain == Terrain.GRASS) return 2;
            if (fromTerrain == Terrain.GRASS && toTerrain == Terrain.MOUNTAIN) return 3;
            if (fromTerrain == Terrain.MOUNTAIN && toTerrain == Terrain.GRASS) return 3;
            if (fromTerrain == Terrain.MOUNTAIN && toTerrain == Terrain.MOUNTAIN) return 4;

            System.err.println("MovementCostProfile.SHORTEST_PATH: Unhandled terrain transition from " + fromTerrain + " to " + toTerrain);
            return Integer.MAX_VALUE;
        }
    };

    abstract int cost(MapNode from, MapNode to);
}
