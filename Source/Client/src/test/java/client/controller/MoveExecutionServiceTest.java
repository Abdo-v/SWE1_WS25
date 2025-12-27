package client.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import client.controller.network.service.NetworkCenter;
import client.exception.AIDecisionException;
import client.exception.GameCommunicationException;
import client.exception.GameStateException;
import client.exception.NoValidMoveAvailableException;
import client.model.Direction;
import client.model.GameMode;
import client.model.GameState;
import client.model.PlayerState;
import client.model.PlayerStatus;
import client.model.ai.WayFinder;
import client.model.mapper.MapNode;
import client.model.mapper.Terrain;
import client.view.GameOutput;

/**
 * Unit tests for {@link MoveExecutionService}.
 */
class MoveExecutionServiceTest {

    @Test
    void makeMove_whenGameStateNull_throwsGameStateException() {
        MoveExecutionService service = new MoveExecutionService(mock(NetworkCenter.class), mock(WayFinder.class), mock(GameOutput.class));

        assertThrows(GameStateException.class, () -> service.makeMove(null, "p1", GameMode.UNKNOWN));
    }

    @Test
    void makeMove_whenCurrentPlayerStateMissing_throwsGameStateException() {
        MoveExecutionService service = new MoveExecutionService(mock(NetworkCenter.class), mock(WayFinder.class), mock(GameOutput.class));
        GameState state = new GameState("gs-1");

        assertThrows(GameStateException.class, () -> service.makeMove(state, "p1", GameMode.UNKNOWN));
    }

    @Test
    void makeMove_whenWayFinderReturnsNull_throwsAIDecisionException() throws Exception {
        NetworkCenter networkCenter = mock(NetworkCenter.class);
        WayFinder wayFinder = mock(WayFinder.class);
        when(wayFinder.findNext()).thenReturn(null);

        MoveExecutionService service = new MoveExecutionService(networkCenter, wayFinder, mock(GameOutput.class));

        GameState state = newGameStateWithSinglePlayerAt("gs-2", 0, 0);
        assertThrows(AIDecisionException.class, () -> service.makeMove(state, "p1", GameMode.UNKNOWN));
    }

    @Test
    void makeMove_whenNoValidMoveButFallbackProvided_sendsFallbackAndShowsError() throws Exception {
        NetworkCenter networkCenter = mock(NetworkCenter.class);
        WayFinder wayFinder = mock(WayFinder.class);
        GameOutput output = mock(GameOutput.class);

        when(wayFinder.findNext()).thenThrow(new NoValidMoveAvailableException(
                "no moves",
                "WayFinder",
                "findNext",
                Optional.of(Direction.LEFT)
        ));

        MoveExecutionService service = new MoveExecutionService(networkCenter, wayFinder, output);
        GameState state = newGameStateWithSinglePlayerAt("gs-3", 1, 1);

        service.makeMove(state, "p1", GameMode.TRR);

        verify(output, times(1)).showAiError(org.mockito.ArgumentMatchers.contains("Falling back"));
        verify(networkCenter, times(1)).sendMove(Direction.LEFT);
        verify(output, times(1)).showMoveSent(Direction.LEFT);
    }

    @Test
    void makeMove_whenNoValidMoveAndNoFallback_throwsAIDecisionException() throws Exception {
        NetworkCenter networkCenter = mock(NetworkCenter.class);
        WayFinder wayFinder = mock(WayFinder.class);
        when(wayFinder.findNext()).thenThrow(new NoValidMoveAvailableException(
                "no moves",
                "WayFinder",
                "findNext",
                Optional.empty()
        ));

        MoveExecutionService service = new MoveExecutionService(networkCenter, wayFinder, mock(GameOutput.class));
        GameState state = newGameStateWithSinglePlayerAt("gs-4", 1, 1);

        assertThrows(AIDecisionException.class, () -> service.makeMove(state, "p1", GameMode.UNKNOWN));
    }

    @Test
    void makeMove_whenNetworkCenterThrowsRuntimeException_wrapsIntoGameCommunicationException() throws Exception {
        NetworkCenter networkCenter = mock(NetworkCenter.class);
        WayFinder wayFinder = mock(WayFinder.class);
        when(wayFinder.findNext()).thenReturn(Direction.UP);
        doThrow(new RuntimeException("socket closed")).when(networkCenter).sendMove(Direction.UP);

        MoveExecutionService service = new MoveExecutionService(networkCenter, wayFinder, mock(GameOutput.class));
        GameState state = newGameStateWithSinglePlayerAt("gs-5", 1, 1);

        GameCommunicationException ex = assertThrows(
                GameCommunicationException.class,
                () -> service.makeMove(state, "p1", GameMode.UNKNOWN)
        );
        assertEquals(-1, ex.getHttpStatusCode());
    }

    private static GameState newGameStateWithSinglePlayerAt(String gameStateId, int x, int y) {
        GameState state = new GameState(gameStateId);
        state.addPlayer(new PlayerState(
                "p1",
                "f",
                "l",
                "u",
                false,
                new MapNode(x, y, Terrain.GRASS, false, false),
                PlayerStatus.MUST_ACT
        ));
        return state;
    }
}
