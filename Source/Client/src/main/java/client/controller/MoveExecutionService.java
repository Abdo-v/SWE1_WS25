package client.controller;

import client.controller.network.service.NetworkCenter;
import client.exception.AIDecisionException;
import client.exception.FailureReason;
import client.exception.GameCommunicationException;
import client.exception.GameStateException;
import client.exception.Operation;
import client.model.Direction;
import client.model.GameMode;
import client.model.GameState;
import client.model.ai.WayFinder;
import client.view.GameOutput;

final class MoveExecutionService {

    private final NetworkCenter networkCenter;
    private final WayFinder wayFinder;
    private final GameOutput output;

    MoveExecutionService(NetworkCenter networkCenter, WayFinder wayFinder, GameOutput output) {
        this.networkCenter = networkCenter;
        this.wayFinder = wayFinder;
        this.output = output;
    }

    void makeMove(GameState gameState, String playerId, GameMode gameMode)
            throws GameCommunicationException, AIDecisionException, GameStateException {

        if (gameState == null || gameState.getCurrentPlayerState() == null) {
            throw new GameStateException(
                    "Cannot make move: game state or player state is null",
                    gameState != null ? gameState.getGameStateID() : "unknown",
                    "MAKE_MOVE",
                    "invalid_state"
            );
        }

        try {
            Direction nextMoveDirection = wayFinder.findNext();
            if (nextMoveDirection == null) {
                throw new AIDecisionException(
                        "WayFinder failed to determine a valid move",
                        "WayFinder",
                        "findNext",
                        gameState.getCurrentPlayerState().getCurrentPosition()
                );
            }

            try {
                networkCenter.sendMove(nextMoveDirection);

                if (gameMode != null && gameMode.isReduced()) {
                    output.showMoveSent(nextMoveDirection);
                }
            } catch (GameCommunicationException e) {
                throw e;
            } catch (Exception e) {
                throw new GameCommunicationException(
                        "Failed to send move to server: " + e.getMessage(),
                        e,
                        networkCenter != null ? FailureReason.UNKNOWN.code() : FailureReason.NO_NETWORK.code(),
                        Operation.SEND_MOVE,
                        -1
                );
            }

        } catch (AIDecisionException | GameCommunicationException e) {
            throw e;
        } catch (Exception e) {
            throw new GameStateException(
                    "Unexpected error during move making: " + e.getMessage(),
                    e,
                    gameState.getGameStateID(),
                    Operation.MAKE_MOVE,
                    FailureReason.ERROR,
                    null
            );
        }
    }
}
