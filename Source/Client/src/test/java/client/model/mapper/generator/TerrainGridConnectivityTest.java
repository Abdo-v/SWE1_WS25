package client.model.mapper.generator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import client.model.mapper.Terrain;

/**
 * Unit tests for {@link TerrainGridConnectivity}.
 *
 * <p>Connectivity is a hard precondition for accepting a generated map:
 * all non-water tiles must be mutually reachable via cardinal moves.
 */
class TerrainGridConnectivityTest {

    @Test
    void checkConnectivity_whenAllTilesWalkable_returnsTrue() {
        int w = 4;
        int h = 3;
        Terrain[][] grid = all(Terrain.GRASS, w, h);
        assertTrue(TerrainGridConnectivity.checkConnectivity(grid, w, h));
    }

    @Test
    void checkConnectivity_whenAllTilesWater_returnsFalse() {
        int w = 4;
        int h = 3;
        Terrain[][] grid = all(Terrain.WATER, w, h);
        assertFalse(TerrainGridConnectivity.checkConnectivity(grid, w, h));
    }

    @Test
    void checkConnectivity_whenLandIsSplitIntoIslands_returnsFalse() {
        int w = 4;
        int h = 3;
        Terrain[][] grid = all(Terrain.WATER, w, h);

        // Two isolated grass tiles that cannot reach each other.
        grid[0][0] = Terrain.GRASS;
        grid[3][2] = Terrain.GRASS;

        assertFalse(TerrainGridConnectivity.checkConnectivity(grid, w, h));
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
}
