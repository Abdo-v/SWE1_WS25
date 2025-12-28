package client.controller;

import client.exception.AIDecisionException;
import client.exception.FailureReason;
import client.exception.Operation;
import client.exception.GameCommunicationException;
import client.exception.GameStateException;
import client.exception.MapProcessingException;
import client.model.GameMode;
import client.model.PlayerState;
import client.model.PlayerStatus;
import client.model.common.DebugSettings;
import client.view.GameOutput;

import java.time.Duration;
import java.util.Objects;

/**
 * Drives the turn-by-turn game loop: poll state, decide/make moves, and render output.
 *
 * <p>In dynamic modes, enables dynamic visualization after a short startup delay.
 */
class GameLoopService {

    private static final Duration DYNAMIC_VISUALIZATION_START_DELAY = Duration.ofSeconds(1);

    private final GameManager gameManager;
    private final GameOutput output;

    public GameLoopService(GameManager gameManager, GameOutput output) {
        this.gameManager = Objects.requireNonNull(gameManager, ControllerTextConfig.REQUIRE_GAME_MANAGER);
        this.output = ControllerTextConfig.defaultIfMissing(output, new NoOpGameOutput());
    }

    /**
     * Runs the main loop until the server reports a terminal player status.
     *
     * <p>Recoverable network and map-processing failures are reported and the loop continues;
     * non-recoverable failures are re-thrown.
     */
    public void startGameLoop(String gameMode) throws GameCommunicationException, GameStateException {
        startGameLoop(GameMode.fromCLIValue(gameMode));
    }

    private void startGameLoop(GameMode gameMode) throws GameCommunicationException, GameStateException {
        GameMode effectiveMode = ControllerTextConfig.defaultIfMissing(gameMode, GameMode.UNKNOWN);
        boolean dynamicMode = effectiveMode.isDynamicVisualization();
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
                            gameManager.makeMove(effectiveMode);
                            if (effectiveMode.isReduced()) {
                                gameManager.getGameState().getCurrentPlayerState()
                                        .flatMap(PlayerState::getCurrentPosition)
                                        .map(pos -> pos.printCoordinates())
                                        .ifPresent(output::showPosition);
                            }
                            acted = true;
                        } catch (AIDecisionException e) {
                            output.showAiError(e.getMessage());
                            DebugSettings.printStackTraceIfDebug(e);
                        } catch (GameCommunicationException e) {
                            output.showNetworkError(e.getMessage());
                            DebugSettings.printStackTraceIfDebug(e);
                            if (!e.isRecoverable()) {
                                throw e;
                            }
                        }
                        break;
                    case WON:
                        gameManager.disableDynamicVisualization();
                        output.showWon(
                                gameManager.getGameState().getCurrentPlayerState().orElseThrow(),
                                loops
                        );
                        gameIsRunning = false;
                        break;
                    case LOST:
                        gameManager.disableDynamicVisualization();
                        output.showLost(
                                gameManager.getGameState().getCurrentPlayerState().orElseThrow(),
                                loops
                        );
                        gameIsRunning = false;
                        break;
                    default:
                        gameManager.disableDynamicVisualization();
                        output.showUnhandledStatus(currentStatus);
                        gameIsRunning = false;
                        break;
                }

                loops++;
                if (effectiveMode.isReduced()) {
                    output.showLoops(loops);
                }

            } catch (MapProcessingException e) {
                output.showMapError(e.getRecoveryMessage());
                DebugSettings.printStackTraceIfDebug(e);
                if (!e.isRecoverable()) {
                    throw new GameStateException(
                        ControllerTextConfig.ERROR_FATAL_MAP_PROCESSING_PREFIX + e.getMessage(),
                        e,
                        gameManager.getGameState().getGameStateID(),
                        Operation.GAME_LOOP.code(),
                        FailureReason.MAP_ERROR.code(),
                        ""
                    );
                }
            } catch (GameCommunicationException | GameStateException e) {
                throw e;
            } catch (Exception e) {
                DebugSettings.printStackTraceIfDebug(e);
                throw new GameStateException(
                    ControllerTextConfig.ERROR_UNEXPECTED_GAME_LOOP_PREFIX + e.getMessage(),
                    e,
                    gameManager.getGameState().getGameStateID(),
                    Operation.GAME_LOOP.code(),
                    FailureReason.UNEXPECTED_ERROR.code(),
                    ""
                );
            }
        }

        gameManager.disableDynamicVisualization();
    }
}

