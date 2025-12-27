package client.controller;

import client.exception.GameStateException;
import client.model.GameState;
import client.model.PlayerState;
import client.model.PlayerStatus;
import client.model.mapper.MapNode;
import client.model.mapper.Terrain;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link PlayerTurnService} status resolution for the current player within a given game state.
 */
class PlayerTurnServiceTest {

    @Test
    void getCurrentPlayerStatus_whenGameStateMissing_throwsGameStateException() {
        PlayerTurnService service = new PlayerTurnService();

        assertThrows(GameStateException.class, () -> service.getCurrentPlayerStatus(null, "p1"));
    }

    @Test
    void getCurrentPlayerStatus_whenPlayerFound_returnsStatus() throws Exception {
        PlayerTurnService service = new PlayerTurnService();

        GameState state = new GameState("id");
        MapNode pos = new MapNode(0, 0, Terrain.GRASS, false, false);
        PlayerState player = new PlayerState("p1", "A", "B", "u", false, pos, PlayerStatus.MUST_ACT);
        state.addPlayer(player);

        assertEquals(PlayerStatus.MUST_ACT, service.getCurrentPlayerStatus(state, "p1"));
    }

    @Test
    void getCurrentPlayerStatus_whenPlayerNotFound_throwsGameStateException() {
        PlayerTurnService service = new PlayerTurnService();

        GameState state = new GameState("id");
        state.addPlayer(new PlayerState("p1", "A", "B", "u"));

        GameStateException ex = assertThrows(GameStateException.class, () -> service.getCurrentPlayerStatus(state, "missing"));
        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
    }
}
