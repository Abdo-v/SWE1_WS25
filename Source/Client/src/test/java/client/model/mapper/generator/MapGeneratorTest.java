package client.model.mapper.generator;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MapGenerator}.
 *
 * <p>We keep these tests deterministic and fast by focusing on fail-fast argument validation.
 * Full map generation itself is probabilistic (randomized) and therefore not a good unit-test target
 * without explicit dependency injection hooks.
 */
class MapGeneratorTest {

    @Test
    void generateMap_whenWidthNotPositive_throwsIllegalArgumentException() {
        MapGenerator gen = new MapGenerator();
        assertThrows(IllegalArgumentException.class, () -> gen.generateMap(0, 5, "p1"));
    }

    @Test
    void generateMap_whenHeightNotPositive_throwsIllegalArgumentException() {
        MapGenerator gen = new MapGenerator();
        assertThrows(IllegalArgumentException.class, () -> gen.generateMap(10, 0, "p1"));
    }

}
