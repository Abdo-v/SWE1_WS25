package client.model.ai;

import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.OwnToOppMapOrientation;
import client.model.mapper.PlayerHalfMap;
import client.model.mapper.Terrain;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit tests for {@link TraversalWayBuilders}.
 *
 * <p>Validates traversal structure creation and the "unvisited" convention (value != true).
 */
class TraversalWayBuildersTest {

    /**
     * Ensures grass traversal contains only grass nodes and initializes them as unvisited.
     */
    @Test
    void grassTraversal_includesOnlyGrassAndMarksUnvisited() {
        PlayerHalfMap halfMap = new PlayerHalfMap();

        MapNode grass = new MapNode(0, 0, Terrain.GRASS, false, false);
        MapNode water = new MapNode(1, 0, Terrain.WATER, false, false);
        MapNode mountain = new MapNode(2, 0, Terrain.MOUNTAIN, false, false);

        halfMap.addMapNode(grass);
        halfMap.addMapNode(water);
        halfMap.addMapNode(mountain);

        LinkedHashMap<MapNode, Boolean> traversal = TraversalWayBuilders.grassTraversal(halfMap);

        assertEquals(1, traversal.size());
        assertFalse(traversal.get(grass));
    }

    /**
     * Ensures mountain field collection contains only mountains and initializes them as unvisited.
     */
    @Test
    void mountainFields_includesOnlyMountainsAndMarksUnvisited() {
        ArrayList<MapNode> nodes = new ArrayList<>();
        MapNode grass = new MapNode(0, 0, Terrain.GRASS, false, false);
        MapNode mountain = new MapNode(1, 0, Terrain.MOUNTAIN, false, false);
        nodes.add(grass);
        nodes.add(mountain);

        GameMap map = new GameMap(nodes, OwnToOppMapOrientation.UP_DOWN, 9, 9);

        LinkedHashMap<MapNode, Boolean> mountains = TraversalWayBuilders.mountainFields(map);

        assertEquals(1, mountains.size());
        assertFalse(mountains.get(mountain));
    }

    /**
     * Ensures the "unvisited" convention includes values that are false or null, but excludes true.
     */
    @Test
    void toUnvisitedNodes_includesFalseAndNull_excludesTrue() {
        MapNode a = new MapNode(0, 0, Terrain.GRASS, false, false);
        MapNode b = new MapNode(1, 0, Terrain.GRASS, false, false);
        MapNode c = new MapNode(2, 0, Terrain.GRASS, false, false);

        LinkedHashMap<MapNode, Boolean> visited = new LinkedHashMap<>();
        visited.put(a, false);
        visited.put(b, null);
        visited.put(c, true);

        var unvisited = TraversalWayBuilders.toUnvisitedNodes(visited);

        assertEquals(2, unvisited.size());
        assertEquals(true, unvisited.contains(a));
        assertEquals(true, unvisited.contains(b));
    }
}
