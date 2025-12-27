package client.model.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.OwnToOppMapOrientation;
import client.model.mapper.PlayerHalfMap;
import client.model.mapper.Terrain;

/**
 * Unit tests for {@link HalfMapSnakeArranger} verifying deterministic traversal ordering.
 */
class HalfMapSnakeArrangerTest {

    /**
     * When the player is on the left side of their half, own-half arrangement should scan X left-to-right,
     * alternating Y direction per column (snake traversal).
     */
    @Test
    void arrangeOwnHalf_whenPlayerOnLeft_scansColumnsLeftToRight_withYSnakeOrdering() {
        GameMap map = buildFullMap(OwnToOppMapOrientation.LEFT_RIGHT);
        MapNode current = map.getNode(0, 0);

        PlayerHalfMap arranged = HalfMapSnakeArranger.arrangeOwnHalf(map, current);

        // First column (x=0) should go top-to-bottom (y=0..4)
        assertEquals(0, arranged.getMapNodes().get(0).getX());
        assertEquals(0, arranged.getMapNodes().get(0).getY());
        assertEquals(0, arranged.getMapNodes().get(4).getX());
        assertEquals(4, arranged.getMapNodes().get(4).getY());

        // Next column (x=1) should flip: bottom-to-top starts at (1,4)
        assertEquals(1, arranged.getMapNodes().get(5).getX());
        assertEquals(4, arranged.getMapNodes().get(5).getY());
    }

    /**
     * When the player is on the right side of their half, own-half arrangement should scan X right-to-left.
     */
    @Test
    void arrangeOwnHalf_whenPlayerOnRight_scansColumnsRightToLeft() {
        GameMap map = buildFullMap(OwnToOppMapOrientation.LEFT_RIGHT);
        MapNode current = map.getNode(9, 0);

        PlayerHalfMap arranged = HalfMapSnakeArranger.arrangeOwnHalf(map, current);

        // First column should start at x=9
        assertEquals(9, arranged.getMapNodes().get(0).getX());
        assertEquals(0, arranged.getMapNodes().get(0).getY());
    }

    /**
     * Opponent-half arrangement for LEFT_RIGHT should iterate over x in the opponent half (10..19),
     * with snake ordering; direction depends on player position thresholds.
     */
    @Test
    void arrangeOpponentHalf_whenPlayerOnLeft_scansOpponentColumnsLeftToRight() {
        GameMap map = buildFullMap(OwnToOppMapOrientation.LEFT_RIGHT);
        MapNode current = map.getNode(0, 0);

        PlayerHalfMap arranged = HalfMapSnakeArranger.arrangeOpponentHalf(map, current);

        assertEquals(10, arranged.getMapNodes().get(0).getX());
        assertEquals(0, arranged.getMapNodes().get(0).getY());
    }

    /**
     * Null preconditions should fail fast with {@link IllegalArgumentException}.
     */
    @Test
    void arrangeOwnHalf_whenMapIsNull_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> HalfMapSnakeArranger.arrangeOwnHalf(null, new MapNode()));
    }

    /**
     * Null preconditions should fail fast with {@link IllegalArgumentException}.
     */
    @Test
    void arrangeOwnHalf_whenCurrentPositionIsNull_throwsIllegalArgumentException() {
        GameMap map = buildFullMap(OwnToOppMapOrientation.LEFT_RIGHT);
        assertThrows(IllegalArgumentException.class, () -> HalfMapSnakeArranger.arrangeOwnHalf(map, null));
    }

    private static GameMap buildFullMap(OwnToOppMapOrientation orientation) {
        ArrayList<MapNode> nodes = new ArrayList<>();
        int maxX;
        int maxY;

        if (orientation == OwnToOppMapOrientation.LEFT_RIGHT || orientation == OwnToOppMapOrientation.RIGHT_LEFT) {
            maxX = 19;
            maxY = 4;
        } else {
            maxX = 9;
            maxY = 9;
        }

        for (int y = 0; y <= maxY; y++) {
            for (int x = 0; x <= maxX; x++) {
                nodes.add(new MapNode(x, y, Terrain.GRASS, false, false));
            }
        }

        return new GameMap(nodes, orientation, maxX, maxY);
    }
}
