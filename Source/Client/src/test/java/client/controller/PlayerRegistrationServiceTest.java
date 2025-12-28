package client.controller;

import client.controller.network.service.NetworkCenter;
import client.exception.GameCommunicationException;
import client.exception.GameStateException;
import client.model.GameState;
import messagesbase.UniquePlayerIdentifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests {@link PlayerRegistrationService} registration flow and error handling around missing/invalid game state.
 */
class PlayerRegistrationServiceTest {

    @Test
    void registerPlayer_whenNetworkSucceeds_addsPlayerAndReturnsId() throws Exception {
        NetworkCenter networkCenter = mock(NetworkCenter.class);
        PlayerRegistrationService service = new PlayerRegistrationService(networkCenter);

        UniquePlayerIdentifier id = mock(UniquePlayerIdentifier.class);
        when(id.getUniquePlayerID()).thenReturn("p1");
        when(networkCenter.registerPlayer("A", "B", "u")).thenReturn(id);

        GameState state = new GameState("game");

        String returned = service.registerPlayer(state, "A", "B", "u");

        assertEquals("p1", returned);
        assertEquals(1, state.getPlayers().size());
        assertEquals("p1", state.getPlayers().get(0).getPlayerID());
    }

    @Test
    void registerPlayer_whenNetworkThrowsGameCommunicationException_propagates() throws Exception {
        NetworkCenter networkCenter = mock(NetworkCenter.class);
        PlayerRegistrationService service = new PlayerRegistrationService(networkCenter);

        when(networkCenter.registerPlayer(anyString(), anyString(), anyString()))
                .thenThrow(new GameCommunicationException("net", "op", "reason", -1));

        GameState state = new GameState("game");

        assertThrows(GameCommunicationException.class, () -> service.registerPlayer(state, "A", "B", "u"));
    }

    @Test
    void registerPlayer_whenUnexpectedException_wrapsAsGameStateException() throws Exception {
        NetworkCenter networkCenter = mock(NetworkCenter.class);
        PlayerRegistrationService service = new PlayerRegistrationService(networkCenter);

        when(networkCenter.registerPlayer(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("boom"));

        GameState state = new GameState("game");

        GameStateException ex = assertThrows(GameStateException.class, () -> service.registerPlayer(state, "A", "B", "u"));
        assertTrue(ex.getMessage().toLowerCase().contains("unexpected"));
    }
}
