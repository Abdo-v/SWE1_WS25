package client.controller;

import client.exception.GameStateException;
import client.model.GameState;
import client.model.PlayerStatus;

import java.util.Optional;

final class PlayerTurnService {

    PlayerStatus getCurrentPlayerStatus(GameState gameState, String playerId) throws GameStateException {
        GameState state = Optional.ofNullable(gameState).orElseThrow(() -> new GameStateException(
            "Cannot get player status: game state is missing",
            "unknown",
            "GET_PLAYER_STATUS",
            "missing"
        ));

        var players = Optional.ofNullable(state.getPlayers()).orElseThrow(() -> new GameStateException(
            "Cannot get player status: players list is missing",
            state.getGameStateID(),
            "GET_PLAYER_STATUS",
            "missing_players"
        ));

        for (client.model.PlayerState playerState : players) {
            if (playerState.getPlayerID().equals(playerId)) {
                return playerState.getStatus();
            }
        }

        throw new GameStateException(
                "Player ID not found in game state",
                state.getGameStateID(),
                "GET_PLAYER_STATUS",
                "player_not_found",
                "player_present"
        );
    }

}

