package client.controller;

import client.controller.network.service.NetworkCenter;
import client.exception.MapProcessingException;
import client.model.GameState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests {@link GameStateSynchronizer} mapping of network/payload failures into domain exceptions.
 */
class GameStateSynchronizerTest {

    @Test
    void synchronize_whenPollingFails_throwsMapProcessingException() throws Exception {
        NetworkCenter networkCenter = mock(NetworkCenter.class);
        when(networkCenter.pollGameState()).thenThrow(new MapProcessingException(
                "Received missing game state from server",
                "GameState",
                "server_response_validation"
        ));

        GameStateSynchronizer synchronizer = new GameStateSynchronizer(networkCenter);
        GameState shared = new GameState("id");

        assertThrows(MapProcessingException.class, () -> synchronizer.synchronize(shared));
    }

    @Test
    void synchronize_whenConversionFails_throwsMapProcessingException() throws Exception {
        NetworkCenter networkCenter = mock(NetworkCenter.class);
        messagesbase.messagesfromserver.GameState serverState = mock(messagesbase.messagesfromserver.GameState.class);
        when(networkCenter.pollGameState()).thenReturn(serverState);

        when(networkCenter.convertServerGamestate(serverState)).thenThrow(new RuntimeException("conversion failed"));

        GameStateSynchronizer synchronizer = new GameStateSynchronizer(networkCenter);
        GameState shared = new GameState("id");

        MapProcessingException ex = assertThrows(MapProcessingException.class, () -> synchronizer.synchronize(shared));
        assertTrue(ex.getMessage().toLowerCase().contains("unexpected"));
    }

    @Test
    void synchronize_whenPollAndConvertOk_updatesSharedState() throws Exception {
        NetworkCenter networkCenter = mock(NetworkCenter.class);
        messagesbase.messagesfromserver.GameState serverState = mock(messagesbase.messagesfromserver.GameState.class);
        when(networkCenter.pollGameState()).thenReturn(serverState);

        GameState polled = new GameState("id");
        polled.setTreasureCollected(true);
        when(networkCenter.convertServerGamestate(serverState)).thenReturn(polled);

        GameStateSynchronizer synchronizer = new GameStateSynchronizer(networkCenter);
        GameState shared = new GameState("id");
        assertFalse(shared.isTreasureCollected());

        synchronizer.synchronize(shared);

        assertTrue(shared.isTreasureCollected());
    }

    @Test
    void synchronize_whenUnexpectedException_wrapsAsMapProcessingException() throws Exception {
        NetworkCenter networkCenter = mock(NetworkCenter.class);
        when(networkCenter.pollGameState()).thenThrow(new RuntimeException("boom"));

        GameStateSynchronizer synchronizer = new GameStateSynchronizer(networkCenter);
        GameState shared = new GameState("id");

        MapProcessingException ex = assertThrows(MapProcessingException.class, () -> synchronizer.synchronize(shared));
        assertTrue(ex.getMessage().toLowerCase().contains("unexpected"));
    }
}
