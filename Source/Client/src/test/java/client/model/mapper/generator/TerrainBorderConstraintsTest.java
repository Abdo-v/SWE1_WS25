package client.model.mapper.generator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import client.model.mapper.Terrain;

/**
 * Unit tests for {@link TerrainBorderConstraints}.
 *
 * <p>These tests verify the edge ratio invariants used by the map generator:
 * each edge must contain at least a minimum amount of walkable tiles and a minimum
 * amount of water tiles.
 */
class TerrainBorderConstraintsTest {

    /**
     * Verifies that a grid meeting the minimum per-edge requirements is accepted.
     */
    @Test
    void checkBorderConstraints_whenEachEdgeMeetsMinWalkableAndMinBlocked_returnsTrue() {
        int width = 10;
        int height = 5;
        Terrain[][] grid = all(Terrain.GRASS, width, height);

        // For width=10: need >=4 walkable and >=2 water on top/bottom.
        // For height=5: need >=2 walkable and >=1 water on left/right.
        setWater(grid, 0, 0);
        setWater(grid, 1, 0);
        setWater(grid, 0, height - 1);
        setWater(grid, 1, height - 1);

        setWater(grid, 0, 1);
        setWater(grid, width - 1, 1);

        assertTrue(TerrainBorderConstraints.checkBorderConstraints(grid, width, height));
    }

    /**
     * Verifies that a grid violating min-walkable (too much water) is rejected.
     */
    @Test
    void checkBorderConstraints_whenTopEdgeHasTooMuchWater_returnsFalse() {
        int width = 10;
        int height = 5;
        Terrain[][] grid = all(Terrain.GRASS, width, height);

        // Make the entire top edge water -> walkable count becomes 0 (< 4).
        for (int x = 0; x < width; x++) {
            setWater(grid, x, 0);
        }

        assertFalse(TerrainBorderConstraints.checkBorderConstraints(grid, width, height));
    }

    private static Terrain[][] all(Terrain terrain, int width, int height) {
        Terrain[][] grid = new Terrain[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = terrain;
            }
        }
        return grid;
    }

    private static void setWater(Terrain[][] grid, int x, int y) {
        grid[x][y] = Terrain.WATER;
    }
}
