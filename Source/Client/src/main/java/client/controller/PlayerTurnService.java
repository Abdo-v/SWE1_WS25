package client.controller;

import client.exception.GameStateException;
import client.model.GameState;
import client.model.PlayerState;
import client.model.PlayerStatus;

import java.util.List;
import java.util.Objects;

/** Resolves the current player's server-reported {@link PlayerStatus} from the shared game state. */
final class PlayerTurnService {

    /**
     * Looks up the matching player entry by id and returns its status.
     *
     * @throws GameStateException if the player list or the player entry is missing.
     */
    PlayerStatus getCurrentPlayerStatus(GameState gameState, String playerId) throws GameStateException {
        GameState state;
        try {
            state = Objects.requireNonNull(gameState);
        } catch (NullPointerException e) {
            throw new GameStateException(
                    ControllerTextConfig.ERROR_CANNOT_GET_PLAYER_STATUS_GAME_STATE_MISSING,
                    ControllerTextConfig.UNKNOWN,
                    ControllerTextConfig.OP_GET_PLAYER_STATUS,
                    ControllerTextConfig.MISSING
            );
        }

        final List<PlayerState> players;
        try {
            players = Objects.requireNonNull(state.getPlayers());
        } catch (NullPointerException e) {
            throw new GameStateException(
                    ControllerTextConfig.ERROR_CANNOT_GET_PLAYER_STATUS_PLAYERS_MISSING,
                    state.getGameStateID(),
                    ControllerTextConfig.OP_GET_PLAYER_STATUS,
                    ControllerTextConfig.REASON_MISSING_PLAYERS
            );
        }

        for (PlayerState playerState : players) {
            if (playerState.getPlayerID().equals(playerId)) {
                return playerState.getStatus();
            }
        }

        throw new GameStateException(
            ControllerTextConfig.ERROR_PLAYER_ID_NOT_FOUND,
                state.getGameStateID(),
            ControllerTextConfig.OP_GET_PLAYER_STATUS,
            ControllerTextConfig.REASON_PLAYER_NOT_FOUND,
            ControllerTextConfig.EXPECTED_PLAYER_PRESENT
        );
    }

}

