package client.model.mapper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlayerHalfMapTest {

    @Test
    void addMapNode_whenExceedingTotalNodes_throws() {
        PlayerHalfMap halfMap = new PlayerHalfMap("p1");

        for (int y = 0; y < HalfMapDimensions.HEIGHT; y++) {
            for (int x = 0; x < HalfMapDimensions.WIDTH; x++) {
                assertTrue(halfMap.addMapNode(new MapNode(x, y, Terrain.GRASS, false, false)));
            }
        }

        assertThrows(IllegalStateException.class, () ->
                halfMap.addMapNode(new MapNode(0, 0, Terrain.GRASS, false, false))
        );
    }
}