package client.model.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import client.model.Direction;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.OwnToOppMapOrientation;
import client.model.mapper.Terrain;

/**
 * Unit tests for {@link GridNavigation}.
 */
class GridNavigationTest {

    @Test
    void getNodeInDirection_whenOutOfBounds_returnsEmptyOptional() {
        GameMap map = build2x2();
        MapNode start = map.getNode(0, 0);

        assertTrueEmpty(GridNavigation.getNodeInDirection(map, start, Direction.LEFT));
        assertTrueEmpty(GridNavigation.getNodeInDirection(map, start, Direction.UP));
    }

    @Test
    void getNodeInDirection_whenInBounds_returnsNode() {
        GameMap map = build2x2();
        MapNode start = map.getNode(0, 0);

        MapNode right = GridNavigation.getNodeInDirection(map, start, Direction.RIGHT).orElseThrow();
        assertEquals(1, right.getX());
        assertEquals(0, right.getY());
    }

    @Test
    void getDirectionToNeighbor_whenDiagonal_returnsEmpty() {
        MapNode current = new MapNode(0, 0, Terrain.GRASS, false, false);
        MapNode diagonal = new MapNode(1, 1, Terrain.GRASS, false, false);

        assertTrueEmpty(GridNavigation.getDirectionToNeighbor(current, diagonal));
    }

    @Test
    void getDirectionToNeighbor_whenDirectNeighbor_returnsDirection() {
        MapNode current = new MapNode(0, 0, Terrain.GRASS, false, false);
        MapNode neighbor = new MapNode(0, 1, Terrain.GRASS, false, false);

        assertEquals(Direction.DOWN, GridNavigation.getDirectionToNeighbor(current, neighbor).orElseThrow());
    }

    private static void assertTrueEmpty(java.util.Optional<?> optional) {
        assertFalse(optional.isPresent());
    }

    private static GameMap build2x2() {
        ArrayList<MapNode> nodes = new ArrayList<>();
        for (int y = 0; y <= 1; y++) {
            for (int x = 0; x <= 1; x++) {
                nodes.add(new MapNode(x, y, Terrain.GRASS, false, false));
            }
        }
        return new GameMap(nodes, OwnToOppMapOrientation.UP_DOWN, 1, 1);
    }
}
