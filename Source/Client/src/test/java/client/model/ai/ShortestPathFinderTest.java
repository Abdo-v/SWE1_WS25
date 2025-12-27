package client.model.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import client.model.Direction;
import client.model.GameState;
import client.model.PlayerState;
import client.model.PlayerStatus;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.OwnToOppMapOrientation;
import client.model.mapper.Terrain;

/**
 * Unit tests for {@link ShortestPathFinder} focusing on defensive behavior and stable edge cases.
 */
class ShortestPathFinderTest {

    @Test
    void findNextValidNodeToTarget_whenGameStateNotSet_throwsIllegalStateException() {
        ShortestPathFinder spf = new ShortestPathFinder();
        assertThrows(IllegalStateException.class, () -> spf.findNextValidNodeToTarget(new MapNode()));
    }

    @Test
    void findNextValidNodeToTarget_whenTargetEqualsCurrent_returnsEmpty() {
        ShortestPathFinder spf = new ShortestPathFinder();

        GameMap map = build3x3AllGrass();
        MapNode current = map.getNode(1, 1);
        GameState state = new GameState("gs-path", new ArrayList<>(List.of(new PlayerState(
                "p1", "f", "l", "u", false, current, PlayerStatus.MUST_ACT
        ))), map);
        spf.update(state);

        assertFalse(spf.findNextValidNodeToTarget(current).isPresent());
    }

    @Test
    void getNodeInDirection_whenOutOfBounds_throwsIllegalArgumentException() {
        ShortestPathFinder spf = new ShortestPathFinder();
        GameMap map = build3x3AllGrass();
        MapNode current = map.getNode(0, 0);
        GameState state = new GameState("gs-path2", new ArrayList<>(List.of(new PlayerState(
                "p1", "f", "l", "u", false, current, PlayerStatus.MUST_ACT
        ))), map);
        spf.update(state);

        assertThrows(IllegalArgumentException.class, () -> spf.getNodeInDirection(current, Direction.LEFT));
    }

    private static GameMap build3x3AllGrass() {
        ArrayList<MapNode> nodes = new ArrayList<>();
        for (int y = 0; y <= 2; y++) {
            for (int x = 0; x <= 2; x++) {
                nodes.add(new MapNode(x, y, Terrain.GRASS, false, false));
            }
        }
        return new GameMap(nodes, OwnToOppMapOrientation.UP_DOWN, 2, 2);
    }
}
