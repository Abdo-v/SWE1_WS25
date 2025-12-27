package client.model.mapper.validator;

import client.model.common.Notification;
import client.model.mapper.MapNode;
import client.model.mapper.PlayerHalfMap;
import client.model.mapper.Terrain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Edge-case tests for {@link MapValidator} focusing on borders/corners and hard invalid placements.
 */
class MapValidatorEdgeCasesTest {

    private static Stream<Arguments> cornerCoordinates() {
        return Stream.of(
                Arguments.of(0, 0),
                Arguments.of(0, 4),
                Arguments.of(9, 0),
                Arguments.of(9, 4)
        );
    }

    @ParameterizedTest
    @MethodSource("cornerCoordinates")
    void validate_allowsFortOnCorner_whenAllOtherRulesSatisfied(int fortX, int fortY) {
        PlayerHalfMap halfMap = buildValidHalfMapWithFortAt(fortX, fortY);

        Notification notification = new MapValidator().validate(halfMap);

        assertFalse(notification.hasErrors(), () -> "Expected corner fort to be valid, but got: " + notification.getErrorMessages());
    }

    @Test
    void validate_rejectsFortOnWater_evenIfCountsAreOtherwiseOk() {
        PlayerHalfMap halfMap = buildValidHalfMapWithFortAt(0, 4);

        // Turn the fort tile into WATER while keeping the fort flag.
        MapNode fortNode = halfMap.getMapNode(0, 4).orElseThrow();
        fortNode.setTerrain(Terrain.WATER);
        fortNode.setFortPresent(true);

        Notification notification = new MapValidator().validate(halfMap);

        assertTrue(notification.hasErrors(), "Expected errors for fort on water");
        assertTrue(
                notification.getErrorMessages().contains("Fort must be placed on a GRASS"),
                () -> "Expected grass-only fort rule to trigger, got: " + notification.getErrorMessages()
        );
    }

    @Test
    void validate_rejectsMapsThatViolateEdgeWalkabilityRatios() {
        // Same terrain totals as a valid map, but move all water away from edges.
        PlayerHalfMap halfMap = buildValidHalfMapWithFortAt(0, 4);

        // Clear existing water pattern by turning it into grass.
        for (int[] pos : DEFAULT_WATER_POSITIONS) {
            halfMap.getMapNode(pos[0], pos[1]).orElseThrow().setTerrain(Terrain.GRASS);
        }

        // Put 7 water tiles strictly inside the map (not on any edge).
        int[][] interiorWater = new int[][]{
            {1, 1}, {2, 1}, {4, 1},
            {1, 2}, {2, 2}, {4, 2},
            {1, 3}
        };
        for (int[] pos : interiorWater) {
            halfMap.getMapNode(pos[0], pos[1]).orElseThrow().setTerrain(Terrain.WATER);
        }

        Notification notification = new MapValidator().validate(halfMap);

        assertTrue(notification.hasErrors(), "Expected edge constraint violations");
        assertTrue(
                notification.getErrorMessages().contains("walkability below threshold")
                        || notification.getErrorMessages().contains("non-walkable fields below threshold"),
                () -> "Expected edge walkability errors, got: " + notification.getErrorMessages()
        );
    }

    // Water pattern that satisfies edge constraints for 10x5 half maps.
    // Works with corner forts because none of these are corners.
    private static final int[][] DEFAULT_WATER_POSITIONS = new int[][]{
            {2, 0}, {7, 0},
            {2, 4}, {7, 4},
            {0, 2}, {9, 2},
            {5, 2}
    };

    private static PlayerHalfMap buildValidHalfMapWithFortAt(int fortX, int fortY) {
        PlayerHalfMap halfMap = new PlayerHalfMap("p1");

        Set<String> water = new HashSet<>();
        for (int[] pos : DEFAULT_WATER_POSITIONS) {
            water.add(pos[0] + "," + pos[1]);
        }

        Set<String> mountains = Set.of(
                "3,1", "3,2", "3,3", "8,1", "8,3"
        );

        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 10; x++) {
                boolean isFort = x == fortX && y == fortY;

                Terrain terrain;
                if (isFort) {
                    terrain = Terrain.GRASS;
                } else if (water.contains(x + "," + y)) {
                    terrain = Terrain.WATER;
                } else if (mountains.contains(x + "," + y)) {
                    terrain = Terrain.MOUNTAIN;
                } else {
                    terrain = Terrain.GRASS;
                }

                halfMap.addMapNode(new MapNode(x, y, terrain, isFort, false));
            }
        }

        // Sanity: fort must exist.
        assertTrue(halfMap.getFortNode().isPresent(), "Precondition failed: fort node must exist");
        return halfMap;
    }
}
