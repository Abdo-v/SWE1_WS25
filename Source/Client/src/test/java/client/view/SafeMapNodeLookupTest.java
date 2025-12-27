package client.view;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.OwnToOppMapOrientation;
import client.model.mapper.Terrain;

/**
 * Tests {@link SafeMapNodeLookup} safe lookup semantics (never throws, returns empty on invalid access).
 */
class SafeMapNodeLookupTest {

    @Test
    void tryGetNode_whenMapThrows_returnsEmpty() {
        GameMap empty = new GameMap();
        assertTrue(SafeMapNodeLookup.tryGetNode(empty, 0, 0).isEmpty());
    }

    @Test
    void tryGetNode_whenNodeExists_returnsPresent() {
        ArrayList<MapNode> nodes = new ArrayList<>();
        nodes.add(new MapNode(0, 0, Terrain.GRASS, false, false));
        GameMap map = new GameMap(nodes, OwnToOppMapOrientation.UP_DOWN, 0, 0);

        assertFalse(SafeMapNodeLookup.tryGetNode(map, 0, 0).isEmpty());
    }
}