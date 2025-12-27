package client.controller;

import client.controller.network.service.NetworkCenter;
import client.exception.GameCommunicationException;
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
    void synchronize_whenSharedGameStateMissing_wrapsAsMapProcessingException() {
        NetworkCenter networkCenter = mock(NetworkCenter.class);
        GameStateSynchronizer synchronizer = new GameStateSynchronizer(networkCenter);

        MapProcessingException ex = assertThrows(MapProcessingException.class, () -> synchronizer.synchronize(null));
        assertNotNull(ex.getCause());
        assertTrue(ex.getCause() instanceof NullPointerException);
    }

    @Test
    void synchronize_whenServerReturnsNullState_throwsMapProcessingException() throws Exception {
        NetworkCenter networkCenter = mock(NetworkCenter.class);
        when(networkCenter.pollGameState()).thenReturn(null);

        GameStateSynchronizer synchronizer = new GameStateSynchronizer(networkCenter);
        GameState shared = new GameState("id");

        assertThrows(MapProcessingException.class, () -> synchronizer.synchronize(shared));
    }

    @Test
    void synchronize_whenConversionReturnsNull_throwsMapProcessingException() throws Exception {
        NetworkCenter networkCenter = mock(NetworkCenter.class);
        messagesbase.messagesfromserver.GameState serverState = mock(messagesbase.messagesfromserver.GameState.class);
        when(networkCenter.pollGameState()).thenReturn(serverState);
        when(networkCenter.convertServerGamestate(serverState)).thenReturn(null);

        GameStateSynchronizer synchronizer = new GameStateSynchronizer(networkCenter);
        GameState shared = new GameState("id");

        assertThrows(MapProcessingException.class, () -> synchronizer.synchronize(shared));
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
