package client.controller;

import client.controller.network.service.NetworkCenter;
import client.exception.GameCommunicationException;
import client.exception.GameStateException;
import client.model.GameState;
import client.model.PlayerState;
import messagesbase.UniquePlayerIdentifier;

final class PlayerRegistrationService {

    private final NetworkCenter networkCenter;

    PlayerRegistrationService(NetworkCenter networkCenter) {
        this.networkCenter = networkCenter;
    }

    String registerPlayer(GameState gameState, String firstName, String lastName, String uAccount)
            throws GameCommunicationException, GameStateException {

        if (gameState == null) {
            throw new GameStateException(
                    "Cannot register player: game state is not initialized",
                    null,
                    "PLAYER_REGISTRATION",
                    "uninitialized"
            );
        }

        try {
            UniquePlayerIdentifier playerIdentifier = networkCenter.registerPlayer(firstName, lastName, uAccount);
            String playerId = playerIdentifier.getUniquePlayerID();
            PlayerState playerState = new PlayerState(playerId, firstName, lastName, uAccount);
            gameState.addPlayer(playerState);
            return playerId;
        } catch (GameCommunicationException e) {
            throw e;
        } catch (Exception e) {
            throw new GameStateException(
                    "Unexpected error during player registration: " + e.getMessage(),
                    e,
                    gameState.getGameStateID(),
                    "PLAYER_REGISTRATION",
                    "unknown",
                    null
            );
        }
    }
}
