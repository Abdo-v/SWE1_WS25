package client.controller;

import client.controller.network.service.NetworkCenter;
import client.exception.FailureReason;
import client.exception.GameCommunicationException;
import client.exception.GameStateException;
import client.exception.Operation;

import java.util.Objects;
import java.util.Optional;

public class GameStateQueryService {

    private final NetworkCenter networkCenter;

    public GameStateQueryService(NetworkCenter networkCenter) {
        this.networkCenter = Objects.requireNonNull(networkCenter, "networkCenter is required");
    }

    public boolean isFullMapAvailable() throws GameCommunicationException {
        try {
            messagesbase.messagesfromserver.GameState serverGameState = pollGameState();
            return Optional.ofNullable(serverGameState.getMap())
                    .map(m -> m.getMapNodes().size() == 100)
                    .orElse(false);
        } catch (GameCommunicationException e) {
            throw e;
        } catch (Exception e) {
            throw new GameCommunicationException(
                "Failed to check full map availability: " + e.getMessage(),
                e,
                FailureReason.UNKNOWN.code(),
                Operation.FULL_MAP_CHECK,
                -1
            );
        }
    }

    public messagesbase.messagesfromserver.GameState pollGameState() throws GameCommunicationException {
        try {
            return networkCenter.pollGameState();
        } catch (GameCommunicationException e) {
            throw e;
        } catch (Exception e) {
            throw new GameCommunicationException(
                "Failed to poll game state from server: " + e.getMessage(),
                e,
                FailureReason.UNKNOWN.code(),
                Operation.POLL_GAME_STATE,
                -1
            );
        }
    }

    public boolean isServerMapEmpty() throws GameCommunicationException {
        try {
            messagesbase.messagesfromserver.GameState serverGameState = pollGameState();
            return Optional.ofNullable(serverGameState.getMap())
                    .map(m -> m.getMapNodes())
                    .map(nodes -> nodes.isEmpty())
                    .orElse(false);
        } catch (GameCommunicationException e) {
            throw e;
        } catch (Exception e) {
            throw new GameCommunicationException(
                "Failed to check if server map is empty: " + e.getMessage(),
                e,
                FailureReason.UNKNOWN.code(),
                Operation.CHECK_SERVER_MAP_EMPTY,
                -1
            );
        }
    }

    public messagesbase.messagesfromserver.EPlayerGameState pollPlayerStatus(String playerId, String gameStateId)
        throws GameCommunicationException, GameStateException {

        try {
            messagesbase.messagesfromserver.GameState serverGameState = pollGameState();

            for (messagesbase.messagesfromserver.PlayerState playerState : serverGameState.getPlayers()) {
                if (playerState.getUniquePlayerID().equals(playerId)) {
                    return playerState.getState();
                }
            }

            throw new GameStateException(
                "Player not found in server game state",
                gameStateId,
                Operation.POLL_PLAYER_STATUS,
                FailureReason.PLAYER_NOT_FOUND,
                FailureReason.PLAYER_PRESENT
            );

        } catch (GameCommunicationException | GameStateException e) {
            throw e;
        } catch (Exception e) {
            throw new GameStateException(
                "Unexpected error polling player status: " + e.getMessage(),
                e,
                gameStateId,
                Operation.POLL_PLAYER_STATUS,
                FailureReason.ERROR,
                FailureReason.UNKNOWN
            );
        }
    }
}

