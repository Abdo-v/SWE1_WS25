package client.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.OwnToOppMapOrientation;
import client.model.mapper.Terrain;
import client.observer.util.Changed;

/**
 * Unit tests for {@link GameState}.
 *
 * <p>Focus:
 * <ul>
 *   <li>Fail-fast preconditions (null handling via {@link java.util.Objects#requireNonNull(Object)})</li>
 *   <li>Event streams (Changed&lt;T&gt;) published when state changes</li>
 *   <li>Derived queries such as {@link GameState#isPlayerInOwnHalfMap()}</li>
 * </ul>
 */
class GameStateTest {

    @Test
    void isPlayerInOwnHalfMap_whenNoPlayerOrMap_returnsFalse() {
        GameState state = new GameState("g1");
        assertFalse(state.isPlayerInOwnHalfMap());
    }

    @Test
    void isPlayerInOwnHalfMap_whenPlayerInOwnHalf_returnsTrue() {
        GameMap map = fullMap(OwnToOppMapOrientation.UP_DOWN);

        MapNode ownHalfNode = new MapNode(0, 0, Terrain.GRASS, false, false);
        PlayerState me = new PlayerState("p1", "A", "B", "u", false, ownHalfNode, PlayerStatus.MUST_ACT);

        ArrayList<PlayerState> players = new ArrayList<>();
        players.add(me);

        GameState state = new GameState("g1", players, map);
        assertTrue(state.isPlayerInOwnHalfMap());
    }

    @Test
    void setTreasureCollected_publishesChangedEventWithOldAndNewValue() {
        GameState state = new GameState("g1");

        List<Changed<Boolean>> changes = new ArrayList<>();
        try (var sub = state.treasureCollectedChanges().subscribe(changes::add)) {
            state.setTreasureCollected(true);
        }

        assertEquals(1, changes.size());
        assertEquals(false, changes.get(0).oldValue());
        assertEquals(true, changes.get(0).newValue());
    }

    @Test
    void setTreasurePosition_publishesChangedEvent() {
        GameState state = new GameState("g1");

        List<Changed<Optional<MapNode>>> changes = new ArrayList<>();
        try (var sub = state.treasurePositionChanges().subscribe(changes::add)) {
            state.setTreasurePosition(new MapNode(1, 1, Terrain.GRASS, false, false));
        }

        assertEquals(1, changes.size());
        assertEquals(Optional.empty(), changes.get(0).oldValue());
        assertTrue(changes.get(0).newValue().isPresent());
        assertEquals("Coordinates: (1, 1)", changes.get(0).newValue().orElseThrow().printCoordinates());
    }

    @Test
    void setOpponentFortPosition_setsOpponentFortFoundTrueAndPublishesEvent() {
        GameState state = new GameState("g1");

        List<Changed<Optional<MapNode>>> changes = new ArrayList<>();
        try (var sub = state.opponentFortPositionChanges().subscribe(changes::add)) {
            state.setOpponentFortPosition(new MapNode(2, 2, Terrain.GRASS, true, false));
        }

        assertTrue(state.isOpponentFortFound());
        assertEquals(1, changes.size());
        assertEquals(Optional.empty(), changes.get(0).oldValue());
        assertTrue(changes.get(0).newValue().isPresent());
    }

    @Test
    void updateGameState_publishesBulkUpdateAndChangedStreams() {
        GameState target = new GameState("g1");

        List<Changed<Boolean>> treasureChanges = new ArrayList<>();
        List<Changed<Boolean>> fortFoundChanges = new ArrayList<>();
        try (var s1 = target.treasureCollectedChanges().subscribe(treasureChanges::add);
             var s2 = target.opponentFortFoundChanges().subscribe(fortFoundChanges::add)) {

            GameMap map = fullMap(OwnToOppMapOrientation.UP_DOWN);
            ArrayList<PlayerState> players = new ArrayList<>();
            players.add(new PlayerState("p1", "A", "B", "u"));

            GameState incoming = new GameState("g1", players, map);
            incoming.setTreasureCollected(true);
            incoming.setOpponentFortFound(true);

            target.updateGameState(incoming);
        }

        assertEquals(1, treasureChanges.size());
        assertEquals(false, treasureChanges.get(0).oldValue());
        assertEquals(true, treasureChanges.get(0).newValue());

        assertEquals(1, fortFoundChanges.size());
        assertEquals(false, fortFoundChanges.get(0).oldValue());
        assertEquals(true, fortFoundChanges.get(0).newValue());
    }

    private static GameMap fullMap(OwnToOppMapOrientation orientation) {
        int width = 20;
        int height = 10;

        ArrayList<MapNode> nodes = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                nodes.add(new MapNode(x, y, Terrain.GRASS, false, false));
            }
        }

        // Max coords are inclusive indices.
        return new GameMap(nodes, orientation, width - 1, height - 1);
    }
}
