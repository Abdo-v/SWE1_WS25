package client.model.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.OwnToOppMapOrientation;
import client.model.mapper.Terrain;

/**
 * Tests {@link GridDijkstra} path cost calculation on small deterministic grids.
 */
class GridDijkstraTest {

    @Test
    void shortestPathCost_onSimpleGrid_returnsExpectedCost() {
        GameMap map = grid(3, 3, List.of());
        MapNode start = new MapNode(0, 0, Terrain.GRASS, false, false);
        MapNode target = new MapNode(2, 0, Terrain.GRASS, false, false);

        int cost = GridDijkstra.shortestPathCost(map, start, target, MovementCostProfile.SHORTEST_PATH);
        assertEquals(4, cost); // 2 steps * 2 cost per grass->grass
    }

    @Test
    void shortestPath_whenBlockedByWater_returnsEmpty() {
        // Block middle row y=1 completely => cannot reach y=2 from y=0
        GameMap map = grid(3, 3, List.of(
                new int[]{0,1}, new int[]{1,1}, new int[]{2,1}
        ));

        MapNode start = new MapNode(0, 0, Terrain.GRASS, false, false);
        MapNode target = new MapNode(0, 2, Terrain.GRASS, false, false);

        assertTrue(GridDijkstra.shortestPath(map, start, target, MovementCostProfile.SHORTEST_PATH).isEmpty());
        assertEquals(Integer.MAX_VALUE, GridDijkstra.shortestPathCost(map, start, target, MovementCostProfile.SHORTEST_PATH));
    }

    @Test
    void shortestPathCosts_whenArgsAreNull_returnsEmptyMap() {
        assertTrue(GridDijkstra.shortestPathCosts(null, null, null).isEmpty());
    }

    private static GameMap grid(int w, int h, List<int[]> waterCells) {
        ArrayList<MapNode> nodes = new ArrayList<>();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Terrain t = Terrain.GRASS;
                for (int[] wc : waterCells) {
                    if (wc[0] == x && wc[1] == y) t = Terrain.WATER;
                }
                nodes.add(new MapNode(x, y, t, false, false));
            }
        }
        return new GameMap(nodes, OwnToOppMapOrientation.UP_DOWN, w - 1, h - 1);
    }
}