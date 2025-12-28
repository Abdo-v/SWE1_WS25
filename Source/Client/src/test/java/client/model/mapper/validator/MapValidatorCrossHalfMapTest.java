package client.model.mapper.validator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import client.model.common.Notification;
import client.model.mapper.HalfMapDimensions;
import client.model.mapper.MapNode;
import client.model.mapper.PlayerHalfMap;
import client.model.mapper.Terrain;

/**
 * Unit tests for cross-half-map validation paths in {@link MapValidator}.
 */
class MapValidatorCrossHalfMapTest {
    /**
     * If the half-map dimensions do not match, validation should fail fast with a clear error message.
     */
    @Test
    void validate_whenDimensionsMismatch_reportsError() {
        PlayerHalfMap newHalfMap = buildValidHalfMap();
        PlayerHalfMap existingHalfMapWithDifferentDimensions = buildHalfMapWithDimensions5x10();

        Notification n = new MapValidator().validate(newHalfMap, existingHalfMapWithDifferentDimensions);

        assertTrue(n.hasErrors());
        assertTrue(n.getErrorMessages().toLowerCase().contains("dimensions mismatch"));
    }

    private static PlayerHalfMap buildValidHalfMap() {
        PlayerHalfMap halfMap = new PlayerHalfMap("p1");

        // Start: all grass
        for (int y = 0; y < HalfMapDimensions.HEIGHT; y++) {
            for (int x = 0; x < HalfMapDimensions.WIDTH; x++) {
                halfMap.addMapNode(new MapNode(x, y, Terrain.GRASS, false, false));
            }
        }

        // Water (7 total) satisfying per-edge constraints
        setTerrain(halfMap, 2, 0, Terrain.WATER);
        setTerrain(halfMap, 7, 0, Terrain.WATER);
        setTerrain(halfMap, 1, 4, Terrain.WATER);
        setTerrain(halfMap, 8, 4, Terrain.WATER);
        setTerrain(halfMap, 0, 2, Terrain.WATER);
        setTerrain(halfMap, 9, 2, Terrain.WATER);
        setTerrain(halfMap, 5, 2, Terrain.WATER);

        // Mountains (>=5)
        setTerrain(halfMap, 3, 1, Terrain.MOUNTAIN);
        setTerrain(halfMap, 4, 1, Terrain.MOUNTAIN);
        setTerrain(halfMap, 5, 1, Terrain.MOUNTAIN);
        setTerrain(halfMap, 6, 1, Terrain.MOUNTAIN);
        setTerrain(halfMap, 3, 3, Terrain.MOUNTAIN);

        // Exactly 1 fort on grass
        setFort(halfMap, 0, 0, true);

        return halfMap;
    }

    private static PlayerHalfMap buildHalfMapWithDimensions5x10() {
        // 5x10 also has 50 nodes, but the dimension util will detect 5x10 vs 10x5.
        PlayerHalfMap halfMap = new PlayerHalfMap("p-existing");
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 5; x++) {
                halfMap.addMapNode(new MapNode(x, y, Terrain.GRASS, false, false));
            }
        }
        return halfMap;
    }

    private static void setTerrain(PlayerHalfMap halfMap, int x, int y, Terrain terrain) {
        MapNode node = halfMap.getMapNode(x, y).orElseThrow();
        node.setTerrain(terrain);
    }

    private static void setFort(PlayerHalfMap halfMap, int x, int y, boolean present) {
        MapNode node = halfMap.getMapNode(x, y).orElseThrow();
        node.setFortPresent(present);
    }
}
