package client.model.ai;

import client.exception.AIDecisionException;
import client.exception.NoValidMoveAvailableException;
import client.model.Direction;
import client.model.GameState;
import client.model.PlayerState;
import client.model.PlayerStatus;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.OwnToOppMapOrientation;
import client.model.mapper.Terrain;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WayFinderCornerFortMoveTest {

    @Test
    void findNext_whenTreasureCollectedAndPlayerOnCornerFort_returnsInBoundsDirection() throws Exception {
        // Arrange: Full map 20x5, own half on the left. Place own fort at bottom-left corner (0,4).
        MapNode fortNode = new MapNode(0, 4, Terrain.GRASS, true, false);

        ArrayList<MapNode> nodes = new ArrayList<>();
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 20; x++) {
                if (x == fortNode.getX() && y == fortNode.getY()) {
                    nodes.add(fortNode);
                } else {
                    nodes.add(new MapNode(x, y, Terrain.GRASS, false, false));
                }
            }
        }

        GameMap map = new GameMap(nodes, OwnToOppMapOrientation.LEFT_RIGHT, 19, 4);

        PlayerState self = new PlayerState(
                "p1",
                "Self",
                "Player",
                "u1",
                true,
                fortNode,
                PlayerStatus.MUST_WAIT
        );

        MapNode enemyPos = map.getNode(18, 4);
        PlayerState enemy = new PlayerState(
                "p2",
                "Enemy",
                "Player",
                "u2",
                false,
                enemyPos,
                PlayerStatus.MUST_WAIT
        );

        GameState state = new GameState("gs", new ArrayList<>(List.of(self, enemy)), map);
        state.setTreasureCollected(true); // Force fort phase.

        WayFinder wayFinder = new WayFinder();
        wayFinder.update(state);

        // Act
        Direction next;
        try {
            next = wayFinder.findNext();
        } catch (NoValidMoveAvailableException e) {
            fail("Expected a valid move from a corner fort, but AI reported none: " + e.getMessage());
            return;
        } catch (AIDecisionException e) {
            fail("Expected AI to handle corner fort, but got AIDecisionException: " + e.getMessage());
            return;
        }

        // Assert: direction stays within bounds.
        int newX = switch (next) {
            case LEFT -> fortNode.getX() - 1;
            case RIGHT -> fortNode.getX() + 1;
            default -> fortNode.getX();
        };
        int newY = switch (next) {
            case UP -> fortNode.getY() - 1;
            case DOWN -> fortNode.getY() + 1;
            default -> fortNode.getY();
        };

        assertTrue(newX >= 0 && newX <= 19, "Move must stay within full-map X bounds");
        assertTrue(newY >= 0 && newY <= 4, "Move must stay within full-map Y bounds");

        // And the destination must exist in the map.
        assertDoesNotThrow(() -> map.getNode(newX, newY));
    }
}
