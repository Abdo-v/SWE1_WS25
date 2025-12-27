package client.controller;

import client.exception.AIDecisionException;
import client.exception.GameCommunicationException;
import client.exception.GameStateException;
import client.exception.MapProcessingException;
import client.model.GameMode;
import client.model.GameState;
import client.model.PlayerState;
import client.model.PlayerStatus;
import client.model.mapper.MapNode;
import client.model.mapper.Terrain;
import client.view.GameOutput;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests {@link GameLoopService} loop behavior across player statuses and recoverable/non-recoverable error paths.
 */
class GameLoopServiceTest {

    @Test
    void startGameLoop_whenMustWaitThenWon_showsWonAndExits() throws Exception {
        GameManager gameManager = mock(GameManager.class);
        GameOutput output = mock(GameOutput.class);

        GameState state = new GameState("id");
        state.addPlayer(new PlayerState("p1", "A", "B", "u"));

        when(gameManager.getGameState()).thenReturn(state);
        when(gameManager.getCurrentPlayerStatus()).thenReturn(PlayerStatus.MUST_WAIT, PlayerStatus.WON);

        GameLoopService service = new GameLoopService(gameManager, output);
        service.startGameLoop(GameMode.UNKNOWN.cliValue());

        verify(gameManager, atLeast(1)).updateGameState();
        verify(output).showWon(any(), anyInt());
        verify(gameManager, atLeastOnce()).disableDynamicVisualization();
    }

    @Test
    void startGameLoop_whenMustActAndAiDecisionFails_reportsAndContinues() throws Exception {
        GameManager gameManager = mock(GameManager.class);
        GameOutput output = mock(GameOutput.class);

        GameState state = new GameState("id");
        state.addPlayer(new PlayerState("p1", "A", "B", "u"));

        when(gameManager.getGameState()).thenReturn(state);
        when(gameManager.getCurrentPlayerStatus()).thenReturn(PlayerStatus.MUST_ACT, PlayerStatus.WON);
        doThrow(new AIDecisionException("ai", "WayFinder", "findNext"))
                .when(gameManager).makeMove(any(GameMode.class));

        GameLoopService service = new GameLoopService(gameManager, output);
        service.startGameLoop(GameMode.UNKNOWN.cliValue());

        verify(output).showAiError(contains("ai"));
        verify(output).showWon(any(), anyInt());
    }

    @Test
    void startGameLoop_whenUpdateGameStateThrowsNonRecoverableMapProcessingException_wrapsToGameStateException() throws Exception {
        GameManager gameManager = mock(GameManager.class);
        GameOutput output = mock(GameOutput.class);

        GameState state = new GameState("id");
        state.addPlayer(new PlayerState("p1", "A", "B", "u"));

        when(gameManager.getGameState()).thenReturn(state);
        // processingStage "validation" => not recoverable
        doThrow(new MapProcessingException("bad", "FullMap", "validation"))
                .when(gameManager).updateGameState();

        GameLoopService service = new GameLoopService(gameManager, output);

        assertThrows(GameStateException.class, () -> service.startGameLoop(GameMode.UNKNOWN.cliValue()));
        verify(output).showMapError(contains("Map processing failed"));
    }

    @Test
    void startGameLoop_whenNetworkErrorNotRecoverable_rethrows() throws Exception {
        GameManager gameManager = mock(GameManager.class);
        GameOutput output = mock(GameOutput.class);

        GameState state = new GameState("id");
        state.addPlayer(new PlayerState("p1", "A", "B", "u"));

        when(gameManager.getGameState()).thenReturn(state);
        when(gameManager.getCurrentPlayerStatus()).thenReturn(PlayerStatus.MUST_ACT);

        // 4xx is NOT recoverable in GameCommunicationException.isRecoverable()
        GameCommunicationException nonRecoverable = new GameCommunicationException("net", "op", "reason", 400);
        doThrow(nonRecoverable).when(gameManager).makeMove(any(GameMode.class));

        assertTimeoutPreemptively(java.time.Duration.ofSeconds(1), () ->
            assertThrows(GameCommunicationException.class,
                () -> new GameLoopService(gameManager, output).startGameLoop(GameMode.UNKNOWN.cliValue())));
        verify(output).showNetworkError(anyString());
    }

    @Test
    void startGameLoop_whenReducedModeAndMustAct_showsPositionAndLoops() throws Exception {
        GameManager gameManager = mock(GameManager.class);
        GameOutput output = mock(GameOutput.class);

        MapNode position = new MapNode(2, 3, Terrain.GRASS, false, false);
        GameState state = new GameState("id");
        state.addPlayer(new PlayerState("p1", "A", "B", "u", false, position, PlayerStatus.MUST_ACT));

        when(gameManager.getGameState()).thenReturn(state);
        when(gameManager.getCurrentPlayerStatus()).thenReturn(PlayerStatus.MUST_ACT, PlayerStatus.WON);
        doNothing().when(gameManager).makeMove(any(GameMode.class));

        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> new GameLoopService(gameManager, output).startGameLoop("TRR"));

        verify(output, atLeastOnce()).showLoops(anyInt());
        verify(output, atLeastOnce()).showPosition(contains("Coordinates"));
        verify(output).showWon(any(), anyInt());
    }

    @Test
    void startGameLoop_whenDynamicModeInterrupted_skipsSleepAndDoesNotEnableDynamicVisualization() throws Exception {
        GameManager gameManager = mock(GameManager.class);
        GameOutput output = mock(GameOutput.class);

        GameState state = new GameState("id");
        state.addPlayer(new PlayerState("p1", "A", "B", "u"));

        when(gameManager.getGameState()).thenReturn(state);
        when(gameManager.getCurrentPlayerStatus()).thenReturn(PlayerStatus.WON);

        // Ensure Thread.sleep() returns immediately (InterruptedException) without waiting 1s.
        Thread.currentThread().interrupt();
        try {
            assertTimeout(Duration.ofSeconds(1), () -> new GameLoopService(gameManager, output).startGameLoop("TR"));
        } finally {
            // Clear interrupt flag for other tests.
            Thread.interrupted();
        }

        verify(output).showDynamicModeStarting();
        verify(gameManager, never()).enableDynamicVisualization();
        verify(output).showWon(any(), anyInt());
    }

    @Test
    void startGameLoop_whenRecoverableMapProcessingException_occurs_thenContinuesAndFinishes() throws Exception {
        GameManager gameManager = mock(GameManager.class);
        GameOutput output = mock(GameOutput.class);

        GameState state = new GameState("id");
        state.addPlayer(new PlayerState("p1", "A", "B", "u"));
        when(gameManager.getGameState()).thenReturn(state);

        // 1st loop: update fails with recoverable stage, should NOT throw.
        // 2nd loop: update ok, status WON => exit.
        doThrow(new MapProcessingException("partial", "FullMap", "conversion"))
                .doNothing()
                .when(gameManager).updateGameState();
        when(gameManager.getCurrentPlayerStatus()).thenReturn(PlayerStatus.WON);

        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> new GameLoopService(gameManager, output).startGameLoop(GameMode.UNKNOWN.cliValue()));

        verify(output, atLeastOnce()).showMapError(contains("recoverable"));
        verify(output).showWon(any(), anyInt());
    }
}
