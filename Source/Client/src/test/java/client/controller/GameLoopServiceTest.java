package client.controller;

import client.exception.AIDecisionException;
import client.exception.GameCommunicationException;
import client.exception.GameStateException;
import client.exception.MapProcessingException;
import client.model.GameMode;
import client.model.GameState;
import client.model.PlayerState;
import client.model.PlayerStatus;
import client.view.GameOutput;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
}
