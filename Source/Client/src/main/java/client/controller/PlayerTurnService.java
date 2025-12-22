package client.controller;

import client.exception.GameStateException;
import client.model.GameState;
import client.model.PlayerStatus;

final class PlayerTurnService {

    PlayerStatus getCurrentPlayerStatus(GameState gameState, String playerId) throws GameStateException {
        if (gameState == null || gameState.getPlayers() == null) {
            throw new GameStateException(
                    "Cannot get player status: game state or players list is null",
                    gameState != null ? gameState.getGameStateID() : "unknown",
                    "GET_PLAYER_STATUS",
                    "null_state"
            );
        }

        for (client.model.PlayerState playerState : gameState.getPlayers()) {
            if (playerState.getPlayerID().equals(playerId)) {
                return playerState.getStatus();
            }
        }

        throw new GameStateException(
                "Player ID not found in game state",
                gameState.getGameStateID(),
                "GET_PLAYER_STATUS",
                "player_not_found",
                "player_present"
        );
    }

    boolean shouldAct(GameState gameState) {
        return gameState.getCurrentPlayerState().getStatus() == PlayerStatus.MUST_ACT;
    }

    boolean shouldWait(GameState gameState) {
        return gameState.getCurrentPlayerState().getStatus() == PlayerStatus.MUST_WAIT;
    }
}
