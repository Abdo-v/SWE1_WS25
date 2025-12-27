package client.model.ai;

import client.model.mapper.MapNode;
import client.model.mapper.Terrain;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link NodeVisitTracker}.
 *
 * <p>Validates how visited flags are written into the correct traversal maps.
 */
class NodeVisitTrackerTest {

    /**
     * Ensures that visiting a grass node in the own half marks it as visited in the own-grass map.
     */
    @Test
    void markVisited_whenOwnHalfGrass_marksOwnGrassMap() {
        WayHelper wayHelper = mock(WayHelper.class);
        LinkedHashMap<MapNode, Boolean> ownGrass = new LinkedHashMap<>();
        when(wayHelper.getHalfMapVisitedGrassFields()).thenReturn(ownGrass);
        when(wayHelper.getOppHalfMapVisitedGrassFields()).thenReturn(new LinkedHashMap<>());
        when(wayHelper.getAllMountainFieldsMap()).thenReturn(new LinkedHashMap<>());

        NodeVisitTracker tracker = new NodeVisitTracker(wayHelper);

        MapNode grass = new MapNode(1, 1, Terrain.GRASS, false, false);
        tracker.markVisited(grass, true);

        assertEquals(Boolean.TRUE, ownGrass.get(grass));
    }

    /**
     * Ensures opponent-half grass marking does not re-introduce nodes into a filtered opponent map.
     */
    @Test
    void markVisited_whenOpponentHalfGrassAndNodeNotInFilteredMap_doesNotAdd() {
        WayHelper wayHelper = mock(WayHelper.class);
        when(wayHelper.getHalfMapVisitedGrassFields()).thenReturn(new LinkedHashMap<>());

        LinkedHashMap<MapNode, Boolean> oppGrass = new LinkedHashMap<>();
        oppGrass.put(new MapNode(0, 0, Terrain.GRASS, false, false), false);
        when(wayHelper.getOppHalfMapVisitedGrassFields()).thenReturn(oppGrass);

        when(wayHelper.getAllMountainFieldsMap()).thenReturn(new LinkedHashMap<>());

        NodeVisitTracker tracker = new NodeVisitTracker(wayHelper);

        MapNode newGrass = new MapNode(2, 2, Terrain.GRASS, false, false);
        tracker.markVisited(newGrass, false);

        assertFalse(oppGrass.containsKey(newGrass));
    }

    /**
     * Ensures that visiting a mountain node marks it as visited in the mountain map.
     */
    @Test
    void markVisited_whenMountain_marksMountainMap() {
        WayHelper wayHelper = mock(WayHelper.class);
        when(wayHelper.getHalfMapVisitedGrassFields()).thenReturn(new LinkedHashMap<>());
        when(wayHelper.getOppHalfMapVisitedGrassFields()).thenReturn(new LinkedHashMap<>());

        LinkedHashMap<MapNode, Boolean> mountains = new LinkedHashMap<>();
        when(wayHelper.getAllMountainFieldsMap()).thenReturn(mountains);

        NodeVisitTracker tracker = new NodeVisitTracker(wayHelper);

        MapNode mountain = new MapNode(3, 3, Terrain.MOUNTAIN, false, false);
        tracker.markVisited(mountain, true);

        assertTrue(Boolean.TRUE.equals(mountains.get(mountain)));
    }
}
