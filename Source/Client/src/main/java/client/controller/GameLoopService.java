package client.controller;

import client.exception.AIDecisionException;
import client.exception.FailureReason;
import client.exception.Operation;
import client.exception.GameCommunicationException;
import client.exception.GameStateException;
import client.exception.MapProcessingException;
import client.model.GameMode;
import client.model.PlayerStatus;
import client.view.GameOutput;

import java.time.Duration;

public class GameLoopService {

    private static final Duration DYNAMIC_VISUALIZATION_START_DELAY = Duration.ofSeconds(1);

    private final GameManager gameManager;
    private final GameOutput output;

    public GameLoopService(GameManager gameManager, GameOutput output) {
        this.gameManager = gameManager;
        this.output = output;
    }

    public void startGameLoop(String gameMode) throws GameCommunicationException, GameStateException {
        startGameLoop(GameMode.fromCliValue(gameMode));
    }

    public void startGameLoop(GameMode gameMode) throws GameCommunicationException, GameStateException {
        boolean dynamicMode = gameMode != null && gameMode.isDynamicVisualization();
        if (dynamicMode) {
            output.showDynamicModeStarting();
            try {
                Thread.sleep(DYNAMIC_VISUALIZATION_START_DELAY.toMillis());
                gameManager.enableDynamicVisualization();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        int loops = 0;
        boolean gameIsRunning = true;
        PlayerStatus currentStatus;

        while (gameIsRunning) {
            try {
                boolean acted = false;
                gameManager.updateGameState();
                currentStatus = gameManager.getCurrentPlayerStatus();

                switch (currentStatus) {
                    case MUST_WAIT:
                        break;
                    case MUST_ACT:
                        try {
                            gameManager.makeMove(gameMode);
                            if (gameMode != null && gameMode.isReduced()) {
                                output.showPosition(gameManager.getGameState().getCurrentPlayerState().getCurrentPosition().printCoordinates());
                            }
                            acted = true;
                        } catch (AIDecisionException e) {
                            output.showAiError(e.getMessage());
                        } catch (GameCommunicationException e) {
                            output.showNetworkError(e.getMessage());
                            if (!e.isRecoverable()) {
                                throw e;
                            }
                        }
                        break;
                    case WON:
                        gameManager.disableDynamicVisualization();
                        output.showWon(gameManager.getGameState().getCurrentPlayerState(), loops, dynamicMode);
                        gameIsRunning = false;
                        break;
                    case LOST:
                        gameManager.disableDynamicVisualization();
                        output.showLost(gameManager.getGameState().getCurrentPlayerState(), loops, dynamicMode);
                        gameIsRunning = false;
                        break;
                    default:
                        gameManager.disableDynamicVisualization();
                        output.showUnhandledStatus(currentStatus);
                        gameIsRunning = false;
                        break;
                }

                loops++;
                if (gameMode != null && gameMode.isReduced()) {
                    output.showLoops(loops);
                }

            } catch (MapProcessingException e) {
                output.showMapError(e.getRecoveryMessage());
                if (!e.isRecoverable()) {
                    throw new GameStateException(
                        "Fatal map processing error: " + e.getMessage(),
                        e,
                        gameManager.getGameState().getGameStateID(),
                        Operation.GAME_LOOP,
                        FailureReason.MAP_ERROR,
                        null
                    );
                }
            } catch (GameCommunicationException | GameStateException e) {
                throw e;
            } catch (Exception e) {
                throw new GameStateException(
                    "Unexpected error in game loop: " + e.getMessage(),
                    e,
                    gameManager.getGameState().getGameStateID(),
                    Operation.GAME_LOOP,
                    FailureReason.UNEXPECTED_ERROR,
                    null
                );
            }
        }

        gameManager.disableDynamicVisualization();
    }
}
