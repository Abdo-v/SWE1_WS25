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
import client.model.PlayerState;
import client.model.ai.WayFinder;
import client.view.GameOutput;

import java.util.Optional;
import java.util.Objects;

final class MoveExecutionService {

    private final NetworkCenter networkCenter;
    private final WayFinder wayFinder;
    private final GameOutput output;

    MoveExecutionService(NetworkCenter networkCenter, WayFinder wayFinder, GameOutput output) {
        this.networkCenter = Objects.requireNonNull(networkCenter, "networkCenter is required");
        this.wayFinder = Objects.requireNonNull(wayFinder, "wayFinder is required");
        this.output = Objects.requireNonNull(output, "output is required");
    }

    void makeMove(GameState gameState, String playerId, GameMode gameMode)
            throws GameCommunicationException, AIDecisionException, GameStateException {

        GameState state = Optional.ofNullable(gameState).orElseThrow(() -> new GameStateException(
            "Cannot make move: game state is missing",
            "unknown",
            "MAKE_MOVE",
            "missing"
        ));

        state.getCurrentPlayerState().orElseThrow(() -> new GameStateException(
            "Cannot make move: current player state is missing",
            state.getGameStateID(),
            "MAKE_MOVE",
            "missing_player_state"
        ));

        GameMode effectiveMode = Objects.requireNonNullElse(gameMode, GameMode.UNKNOWN);

        try {
                Direction nextMoveDirection = Optional.ofNullable(wayFinder.findNext()).orElseThrow(() -> new AIDecisionException(
                    "WayFinder failed to determine a valid move",
                    "WayFinder",
                    "findNext",
                    state.getCurrentPlayerState()
                            .flatMap(PlayerState::getCurrentPosition)
                            .map(Object::toString)
                            .orElse("<unknown_position>")
                ));

            try {
                networkCenter.sendMove(nextMoveDirection);

                if (effectiveMode.isReduced()) {
                    output.showMoveSent(nextMoveDirection);
                }
            } catch (GameCommunicationException e) {
                throw e;
            } catch (Exception e) {
                throw new GameCommunicationException(
                        "Failed to send move to server: " + e.getMessage(),
                        e,
                    FailureReason.UNKNOWN.code(),
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
                    state.getGameStateID(),
                    Operation.MAKE_MOVE.code(),
                    FailureReason.ERROR.code(),
                    ""
            );
        }
    }
}

