package client.model.mapper.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
 

import java.util.Random;

import org.junit.jupiter.api.Test;

import client.model.mapper.Terrain;

/**
 * Unit tests for {@link TerrainGridPlacer}.
 *
 * <p>The placer is intentionally simple: it randomly assigns a terrain type to
 * non-fort cells until a requested count is reached.
 */
class TerrainGridPlacerTest {

    @Test
    void placeTerrain_whenCountIsZero_doesNotChangeGrid() {
        int w = 4;
        int h = 3;
        Terrain[][] grid = all(Terrain.GRASS, w, h);
        boolean[][] fort = new boolean[w][h];

        TerrainGridPlacer.placeTerrain(grid, fort, Terrain.MOUNTAIN, 0, w, h, new Random(1));

        assertEquals(0, count(grid, Terrain.MOUNTAIN, w, h));
    }

    @Test
    void placeTerrain_placesExactlyCountAndNeverOnFortCells() {
        int w = 5;
        int h = 4;
        Terrain[][] grid = all(Terrain.GRASS, w, h);
        boolean[][] fort = new boolean[w][h];

        // Mark a few fort cells that must never become MOUNTAIN.
        fort[0][0] = true;
        fort[1][0] = true;
        fort[2][0] = true;

        TerrainGridPlacer.placeTerrain(grid, fort, Terrain.MOUNTAIN, 4, w, h, new Random(42));

        assertEquals(4, count(grid, Terrain.MOUNTAIN, w, h));
        assertNotEquals(Terrain.MOUNTAIN, grid[0][0]);
        assertNotEquals(Terrain.MOUNTAIN, grid[1][0]);
        assertNotEquals(Terrain.MOUNTAIN, grid[2][0]);
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

    private static int count(Terrain[][] grid, Terrain t, int width, int height) {
        int c = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (grid[x][y] == t) {
                    c++;
                }
            }
        }
        return c;
    }
}
