package client.model.mapper.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import client.model.mapper.MapRules;

/**
 * Unit tests for {@link MapGenerationConfig}.
 *
 * <p>These tests cover the fail-fast validation logic inside the record compact constructor.
 */
class MapGenerationConfigTest {

    @Test
    void defaultConfig_usesMapRulesEdgeWalkableRatio() {
        MapGenerationConfig cfg = MapGenerationConfig.defaultConfig();
        assertEquals(MapRules.MIN_EDGE_WALKABLE_RATIO, cfg.minBorderWalkableRatio());
    }

    @Test
    void constructor_whenCountsNegative_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new MapGenerationConfig(
                -1, 7, 7, 8, 1, 0.4, 50
        ));
    }

    @Test
    void constructor_whenMinGreaterThanMax_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new MapGenerationConfig(
                8, 7, 7, 8, 1, 0.4, 50
        ));
    }

    @Test
    void constructor_whenFortTilesNotPositive_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new MapGenerationConfig(
                5, 7, 7, 8, 0, 0.4, 50
        ));
    }

    @Test
    void constructor_whenMinBorderWalkableRatioOutOfRange_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new MapGenerationConfig(
                5, 7, 7, 8, 1, 0.0, 50
        ));
        assertThrows(IllegalArgumentException.class, () -> new MapGenerationConfig(
                5, 7, 7, 8, 1, 1.1, 50
        ));
    }

    @Test
    void constructor_whenAttemptMultiplierNotPositive_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new MapGenerationConfig(
                5, 7, 7, 8, 1, 0.4, 0
        ));
    }
}
