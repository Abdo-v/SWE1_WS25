package client.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import client.controller.network.service.NetworkCenter;
import client.exception.GameCommunicationException;
import messagesbase.messagesfromserver.FullMap;
import messagesbase.messagesfromserver.GameState;

/**
 * Tests {@link GameStateQueryService} polling logic for detecting full-map availability.
 */
class GameStateQueryServiceTest {

    @Test
    void isFullMapAvailable_returnsTrue_whenServerMapHas100Nodes() throws Exception {
        NetworkCenter networkCenter = mock(NetworkCenter.class);
        GameState serverState = mock(GameState.class);
        FullMap serverMap = mock(FullMap.class);

        when(networkCenter.pollGameState()).thenReturn(serverState);
        when(serverState.getMap()).thenReturn(serverMap);
        when(serverMap.getMapNodes()).thenReturn(new ArrayList<>(Collections.nCopies(100, null)));

        GameStateQueryService service = new GameStateQueryService(networkCenter);

        assertTrue(service.isFullMapAvailable());
        verify(networkCenter, times(1)).pollGameState();
    }

    @Test
    void isFullMapAvailable_wrapsUnexpectedExceptionsIntoGameCommunicationException() throws Exception {
        NetworkCenter networkCenter = mock(NetworkCenter.class);
        when(networkCenter.pollGameState()).thenThrow(new RuntimeException("boom"));

        GameStateQueryService service = new GameStateQueryService(networkCenter);

        assertThrows(GameCommunicationException.class, service::isFullMapAvailable);
        verify(networkCenter, times(1)).pollGameState();
    }
}