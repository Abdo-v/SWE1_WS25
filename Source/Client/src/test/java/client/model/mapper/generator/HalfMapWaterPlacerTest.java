package client.model.mapper.generator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Random;

import org.junit.jupiter.api.Test;

import client.model.mapper.PlayerHalfMap;
import client.model.mapper.Terrain;

/**
 * Unit tests for {@link HalfMapWaterPlacer}.
 *
 * <p>This class is one of the highest-ROI coverage targets: it contains most of the
 * map generation constraints and branching logic, but can still be tested deterministically
 * by using a fixed-seed {@link Random}.
 */
class HalfMapWaterPlacerTest {

    /**
     * Verifies the happy path where the placer can satisfy both
     * edge constraints and global connectivity.
     */
    @Test
    void placeWaterWithConstraints_whenCountMatchesMinimumEdgeRequirement_returnsTrueAndProducesValidGrid() {
        int width = 10;
        int height = 5;

        Terrain[][] grid = all(Terrain.GRASS, width, height);
        boolean[][] fort = new boolean[width][height];

        // For 10x5 the minimum edge water placements are:
        // top: 2, bottom: 2, left: 1, right: 1 => total 6.
        int count = 6;

        boolean ok = HalfMapWaterPlacer.placeWaterWithConstraints(
                grid,
                fort,
                count,
                width,
                height,
                new Random(7),
                Optional.empty(),
                MapGenerationConfig.defaultConfig()
        );

        assertTrue(ok);
        assertTrue(TerrainBorderConstraints.checkBorderConstraints(grid, width, height));
        assertTrue(TerrainGridConnectivity.checkConnectivity(grid, width, height));
    }

    /**
     * Negative test: if the requested count is less than the hard minimum implied by edge rules,
     * the method cannot succeed because it must place edge water first.
     */
    @Test
    void placeWaterWithConstraints_whenCountIsBelowEdgeMinimum_returnsFalse() {
        int width = 10;
        int height = 5;

        Terrain[][] grid = all(Terrain.GRASS, width, height);
        boolean[][] fort = new boolean[width][height];

        // Although each edge has its own minimum, corner tiles overlap edges.
        // On a 10x5 map the unique minimum can be satisfied with 4 corner water tiles.
        // Therefore any requested count < 4 is impossible because the placer must satisfy all edge minima first.
        int impossibleCount = 3;

        boolean ok = HalfMapWaterPlacer.placeWaterWithConstraints(
                grid,
                fort,
                impossibleCount,
                width,
                height,
                new Random(7),
                Optional.empty(),
                MapGenerationConfig.defaultConfig()
        );

        assertFalse(ok);
    }

    /**
     * Verifies that the code path for existing-half-map compatibility does not break placement.
     *
     * <p>This primarily exercises the candidate sorting logic that prefers leaving crossable edges.
     */
    @Test
    void placeWaterWithConstraints_whenExistingHalfMapPresent_stillReturnsTrueForFeasibleCount() {
        int width = 10;
        int height = 5;

        Terrain[][] grid = all(Terrain.GRASS, width, height);
        boolean[][] fort = new boolean[width][height];

        // Create a minimal existing half-map (all grass) so opposite-edge walkability checks have data.
        PlayerHalfMap existing = new PlayerHalfMap("p-existing");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                existing.addMapNode(new client.model.mapper.MapNode(x, y, Terrain.GRASS, false, false));
            }
        }

        boolean ok = HalfMapWaterPlacer.placeWaterWithConstraints(
                grid,
                fort,
                6,
                width,
                height,
                new Random(11),
                Optional.of(existing),
                MapGenerationConfig.defaultConfig()
        );

        assertTrue(ok);
        assertTrue(TerrainBorderConstraints.checkBorderConstraints(grid, width, height));
        assertTrue(TerrainGridConnectivity.checkConnectivity(grid, width, height));
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
