package client.controller.network.service;

import client.exception.GameCommunicationException;
import client.model.Direction;
import client.model.mapper.PlayerHalfMap;
import messagesbase.messagesfromserver.GameState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link NetworkCenter} that validate fail-fast preconditions.
 *
 * <p>These tests are intentionally network-free: they cover branches that throw before any HTTP call is attempted.
 */
class NetworkCenterPreconditionsTest {

    /**
     * Ensures sending a move without registration fails fast.
     */
    @Test
    void sendMove_whenNotRegistered_throwsGameCommunicationException() {
        NetworkCenter networkCenter = new NetworkCenter("http://example.invalid", "game-1");

        GameCommunicationException ex = assertThrows(GameCommunicationException.class,
                () -> networkCenter.sendMove(Direction.UP));

        assertEquals("SEND_MOVE", ex.getOperation().orElseThrow());
        assertEquals(-1, ex.getHttpStatusCode());
    }

    /**
     * Ensures sending a half map without registration fails fast.
     */
    @Test
    void sendHalfMap_whenNotRegistered_throwsGameCommunicationException() {
        NetworkCenter networkCenter = new NetworkCenter("http://example.invalid", "game-1");

        GameCommunicationException ex = assertThrows(GameCommunicationException.class,
                () -> networkCenter.sendHalfMap(new PlayerHalfMap()));

        assertEquals("SEND_HALF_MAP", ex.getOperation().orElseThrow());
        assertEquals(-1, ex.getHttpStatusCode());
    }

    /**
     * Ensures polling game state without registration fails fast.
     */
    @Test
    void pollGameState_whenNotRegistered_throwsGameCommunicationException() {
        NetworkCenter networkCenter = new NetworkCenter("http://example.invalid", "game-1");

        GameCommunicationException ex = assertThrows(GameCommunicationException.class, networkCenter::pollGameState);

        assertEquals("POLL_GAME_STATE", ex.getOperation().orElseThrow());
        assertEquals(-1, ex.getHttpStatusCode());
    }

    /**
     * Ensures converting server game state requires an already known player ID.
     */
    @Test
    void convertServerGamestate_whenNotRegistered_throwsIllegalStateException() {
        NetworkCenter networkCenter = new NetworkCenter("http://example.invalid", "game-1");

        assertThrows(IllegalStateException.class, () -> networkCenter.convertServerGamestate(new GameState("gs")));
    }
}
