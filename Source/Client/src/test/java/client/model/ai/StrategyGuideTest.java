package client.model.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import client.model.GameState;
import client.model.PlayerState;
import client.model.PlayerStatus;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.OwnToOppMapOrientation;
import client.model.mapper.Terrain;

/**
 * Unit tests for {@link StrategyGuide}.
 */
class StrategyGuideTest {

    @Test
    void getGrassNodesFromExtendedVision_whenPositionNull_returnsEmptyList() {
        StrategyGuide guide = new StrategyGuide(mock(WayHelper.class));

        assertTrue(guide.getGrassNodesFromExtendedVision(null).isEmpty());
    }

    @Test
    void getGrassNodesFromExtendedVision_whenMapPresent_returnsOnlyGrassNeighbors() {
        WayHelper wayHelper = mock(WayHelper.class);
        StrategyGuide guide = new StrategyGuide(wayHelper);

        GameMap map = build3x3WithMixedTerrain();
        GameState state = new GameState("gs-vision", new ArrayList<>(List.of(new PlayerState(
                "p1", "f", "l", "u", false, map.getNode(1, 1), PlayerStatus.MUST_ACT
        ))), map);

        guide.update(state);
        verify(wayHelper, times(1)).update(state);

        MapNode center = map.getNode(1, 1);
        ArrayList<MapNode> grass = guide.getGrassNodesFromExtendedVision(center);

        // In the terrain setup below, exactly 3 neighbors are grass.
        assertEquals(3, grass.size());
        assertTrue(grass.stream().allMatch(n -> n.getTerrain() == Terrain.GRASS));
        assertTrue(grass.stream().noneMatch(n -> n.equalsByCoordinates(center)));
    }

    private static GameMap build3x3WithMixedTerrain() {
        ArrayList<MapNode> nodes = new ArrayList<>();
        for (int y = 0; y <= 2; y++) {
            for (int x = 0; x <= 2; x++) {
                Terrain terrain = Terrain.WATER;
                // Make a few grass tiles around the center.
                if ((x == 0 && y == 1) || (x == 1 && y == 0) || (x == 2 && y == 1)) {
                    terrain = Terrain.GRASS;
                }
                // Center doesn't matter for filtering but keep it non-water.
                if (x == 1 && y == 1) {
                    terrain = Terrain.MOUNTAIN;
                }
                nodes.add(new MapNode(x, y, terrain, false, false));
            }
        }

        return new GameMap(nodes, OwnToOppMapOrientation.UP_DOWN, 2, 2);
    }
}
