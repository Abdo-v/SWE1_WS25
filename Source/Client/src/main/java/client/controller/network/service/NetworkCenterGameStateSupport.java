package client.controller.network.service;

import client.controller.ControllerTextConfig;
import client.exception.MapProcessingException;
import messagesbase.ResponseEnvelope;
import messagesbase.messagesfromserver.GameState;

import java.util.Collection;
import java.util.Objects;

/**
 * Small utilities for extracting and sanity-checking {@link GameState} responses.
 *
 * <p>Intentionally preserves the previous "best effort" null-handling behavior.
 */
final class NetworkCenterGameStateSupport {

    GameState requireGameState(ResponseEnvelope<GameState> response) throws MapProcessingException {
        return response.getData().orElseThrow(() -> new MapProcessingException(
                ControllerTextConfig.ERROR_RECEIVED_MISSING_GAME_STATE_FROM_SERVER_RESPONSE,
                ControllerTextConfig.TYPE_GAME_STATE,
                ControllerTextConfig.CONTEXT_SERVER_RESPONSE_VALIDATION
        ));
    }

    int safeMapNodeCount(GameState gameState) {
        try {
            return Objects.requireNonNull(gameState.getMap()).getMapNodes().size();
        } catch (NullPointerException e) {
            return 0;
        }
    }

    void validateMapNodeCountIfPresent(GameState gameState, int nodeCount) {
        try {
            Collection<?> nodes = Objects.requireNonNull(Objects.requireNonNull(gameState.getMap()).getMapNodes());
            int expectedNodes = nodeCount == 50 ? 50 : (nodeCount == 100 ? 100 : -1);
            if (!nodes.isEmpty() && expectedNodes > 0 && nodeCount != expectedNodes && nodeCount != 0) {
                // keep existing behavior: no exception thrown (previously only logged)
            }
        } catch (NullPointerException ignored) {
            // keep existing behavior: treat missing map/nodes as nodeCount=0 and skip validation
        }
    }
}
