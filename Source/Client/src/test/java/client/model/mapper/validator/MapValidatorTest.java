package client.model.mapper.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import client.model.common.Notification;
import client.model.mapper.HalfMapDimensions;
import client.model.mapper.MapNode;
import client.model.mapper.PlayerHalfMap;
import client.model.mapper.Terrain;

/**
 * Tests {@link MapValidator} for common validity conditions (preconditions, duplicates, and structural constraints).
 */
class MapValidatorTest {

    @Test
    void validate_whenHalfMapIsNull_returnsPreconditionError() {
        Notification n = new MapValidator().validate(null);
        assertTrue(n.hasErrors());
        assertTrue(n.getErrorMessages().toLowerCase().contains("must be provided"));
    }

    @Test
    void validate_whenHalfMapIsValid_hasNoErrors() {
        PlayerHalfMap halfMap = buildValidHalfMap();
        Notification n = new MapValidator().validate(halfMap);
        assertFalse(n.hasErrors(), () -> "Expected valid map, errors: " + n.getErrorMessages());
    }

    @Test
    void validate_whenDuplicateCoordinatesExist_reportsError() {
        PlayerHalfMap halfMap = new PlayerHalfMap("p1");

        for (int y = 0; y < HalfMapDimensions.HEIGHT; y++) {
            for (int x = 0; x < HalfMapDimensions.WIDTH; x++) {
                if (x == HalfMapDimensions.WIDTH - 1 && y == HalfMapDimensions.HEIGHT - 1) {
                    halfMap.addMapNode(new MapNode(0, 0, Terrain.GRASS, false, false)); // duplicate
                } else {
                    halfMap.addMapNode(new MapNode(x, y, Terrain.GRASS, false, false));
                }
            }
        }

        Notification n = new MapValidator().validate(halfMap);
        assertTrue(n.hasErrors());
        assertTrue(n.getErrorMessages().contains("Duplicate coordinates"));
    }

    @Test
    void validate_whenCoordinatesIncludeNegative_reportsMissingNodeWithinGrid() {
        PlayerHalfMap halfMap = new PlayerHalfMap("p1");

        for (int y = 0; y < HalfMapDimensions.HEIGHT; y++) {
            for (int x = 0; x < HalfMapDimensions.WIDTH; x++) {
                if (x == HalfMapDimensions.WIDTH - 1 && y == HalfMapDimensions.HEIGHT - 1) {
                    halfMap.addMapNode(new MapNode(-1, 0, Terrain.GRASS, false, false)); // outside, keeps node count at 50
                } else {
                    halfMap.addMapNode(new MapNode(x, y, Terrain.GRASS, false, false));
                }
            }
        }

        Notification n = new MapValidator().validate(halfMap);
        assertTrue(n.hasErrors());
        assertTrue(n.getErrorMessages().contains("start from (0,0)"));
        assertTrue(n.getErrorMessages().contains("Missing map node at coordinates"));
    }

    @Test
    void validate_whenTopEdgeHasNoWater_reportsEdgeConstraintViolation() {
        PlayerHalfMap halfMap = buildValidHalfMapButWithTopEdgeNoWater();
        Notification n = new MapValidator().validate(halfMap);

        assertTrue(n.hasErrors());
        assertTrue(n.getErrorMessages().contains("Top Edge"));
        assertTrue(n.getErrorMessages().contains("non-walkable fields below threshold"));
    }

    private static PlayerHalfMap buildValidHalfMap() {
        PlayerHalfMap halfMap = new PlayerHalfMap("p1");

        // Start: all grass, no fort
        for (int y = 0; y < HalfMapDimensions.HEIGHT; y++) {
            for (int x = 0; x < HalfMapDimensions.WIDTH; x++) {
                halfMap.addMapNode(new MapNode(x, y, Terrain.GRASS, false, false));
            }
        }

        // Water (7 total) satisfying per-edge constraints:
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

    private static PlayerHalfMap buildValidHalfMapButWithTopEdgeNoWater() {
        PlayerHalfMap halfMap = new PlayerHalfMap("p1");

        for (int y = 0; y < HalfMapDimensions.HEIGHT; y++) {
            for (int x = 0; x < HalfMapDimensions.WIDTH; x++) {
                halfMap.addMapNode(new MapNode(x, y, Terrain.GRASS, false, false));
            }
        }

        // Water (7 total), but intentionally NONE on top edge y=0:
        setTerrain(halfMap, 1, 4, Terrain.WATER);
        setTerrain(halfMap, 8, 4, Terrain.WATER);
        setTerrain(halfMap, 0, 2, Terrain.WATER);
        setTerrain(halfMap, 9, 2, Terrain.WATER);
        setTerrain(halfMap, 3, 2, Terrain.WATER);
        setTerrain(halfMap, 6, 2, Terrain.WATER);
        setTerrain(halfMap, 5, 3, Terrain.WATER);

        // Mountains (>=5)
        setTerrain(halfMap, 2, 1, Terrain.MOUNTAIN);
        setTerrain(halfMap, 3, 1, Terrain.MOUNTAIN);
        setTerrain(halfMap, 4, 1, Terrain.MOUNTAIN);
        setTerrain(halfMap, 5, 1, Terrain.MOUNTAIN);
        setTerrain(halfMap, 6, 1, Terrain.MOUNTAIN);

        // 1 fort on grass
        setFort(halfMap, 0, 0, true);

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