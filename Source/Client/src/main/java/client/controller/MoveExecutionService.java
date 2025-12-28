package client.controller;

import client.controller.network.service.NetworkCenter;
import client.exception.AIDecisionException;
import client.exception.FailureReason;
import client.exception.GameCommunicationException;
import client.exception.GameStateException;
import client.exception.NoValidMoveAvailableException;
import client.exception.Operation;
import client.model.Direction;
import client.model.GameMode;
import client.model.GameState;
import client.model.PlayerState;
import client.model.ai.WayFinder;
import client.view.GameOutput;

import java.util.Objects;
import java.util.Optional;

final class MoveExecutionService {

    private final NetworkCenter networkCenter;
    private final WayFinder wayFinder;
    private final GameOutput output;

    MoveExecutionService(NetworkCenter networkCenter, WayFinder wayFinder, GameOutput output) {
        this.networkCenter = Objects.requireNonNull(networkCenter, ControllerTextConfig.REQUIRE_NETWORK_CENTER);
        this.wayFinder = Objects.requireNonNull(wayFinder, ControllerTextConfig.REQUIRE_WAY_FINDER);
        this.output = Objects.requireNonNull(output, ControllerTextConfig.REQUIRE_OUTPUT);
    }

    void makeMove(GameState gameState, GameMode gameMode)
            throws GameCommunicationException, AIDecisionException, GameStateException {

        GameState state;
        try {
            state = Objects.requireNonNull(gameState);
        } catch (NullPointerException e) {
            throw new GameStateException(
                    ControllerTextConfig.ERROR_CANNOT_MAKE_MOVE_GAME_STATE_MISSING,
                    ControllerTextConfig.UNKNOWN,
                    ControllerTextConfig.OP_MAKE_MOVE,
                    ControllerTextConfig.MISSING
            );
        }

        state.getCurrentPlayerState().orElseThrow(() -> new GameStateException(
            ControllerTextConfig.ERROR_CANNOT_MAKE_MOVE_PLAYER_STATE_MISSING,
            state.getGameStateID(),
            ControllerTextConfig.OP_MAKE_MOVE,
            ControllerTextConfig.REASON_MISSING_PLAYER_STATE
        ));

        GameMode effectiveMode = ControllerTextConfig.defaultIfMissing(gameMode, GameMode.UNKNOWN);

        try {
            Direction nextMoveDirection;
            try {
                try {
                    nextMoveDirection = Objects.requireNonNull(wayFinder.findNext(), ControllerTextConfig.ERROR_WAY_FINDER_MISSING_NEXT_MOVE);
                } catch (NullPointerException ignored) {
                    throw new AIDecisionException(
                            ControllerTextConfig.ERROR_WAY_FINDER_MISSING_NEXT_MOVE,
                            ControllerTextConfig.AI_COMPONENT_WAY_FINDER,
                            ControllerTextConfig.AI_CONTEXT_FIND_NEXT,
                            state.getCurrentPlayerState()
                                    .flatMap(PlayerState::getCurrentPosition)
                                    .map(Object::toString)
                                    .orElse(ControllerTextConfig.UNKNOWN_POSITION)
                    );
                }
            } catch (NoValidMoveAvailableException e) {
                Optional<Direction> suggested = e.getSuggestedFallbackDirection();
                if (suggested.isEmpty()) {
                    throw new AIDecisionException(
                            ControllerTextConfig.ERROR_AI_NO_FALLBACK,
                            e,
                            e.getAiComponent().orElse(ControllerTextConfig.AI_COMPONENT_WAY_FINDER),
                            e.getDecisionContext().orElse(ControllerTextConfig.AI_CONTEXT_FIND_NEXT),
                            state.getCurrentPlayerState()
                                    .flatMap(PlayerState::getCurrentPosition)
                                    .map(Object::toString)
                                    .orElse(ControllerTextConfig.UNKNOWN_POSITION)
                    );
                }

                nextMoveDirection = suggested.orElseThrow();
                output.showAiError(
                    ControllerTextConfig.AI_FALLBACK_MESSAGE_PREFIX + e.getMessage() + ControllerTextConfig.AI_FALLBACK_MESSAGE_MIDDLE + nextMoveDirection
                );
            }

            try {
                networkCenter.sendMove(nextMoveDirection);

                if (effectiveMode.isReduced()) {
                    output.showMoveSent(nextMoveDirection);
                }
            } catch (GameCommunicationException e) {
                throw e;
            } catch (Exception e) {
                throw new GameCommunicationException(
                        ControllerTextConfig.ERROR_FAILED_SEND_MOVE_PREFIX + e.getMessage(),
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
                    ControllerTextConfig.ERROR_UNEXPECTED_MOVE_MAKING_PREFIX + e.getMessage(),
                    e,
                    state.getGameStateID(),
                    Operation.MAKE_MOVE.code(),
                    FailureReason.ERROR.code(),
                    ""
            );
        }
    }
}

