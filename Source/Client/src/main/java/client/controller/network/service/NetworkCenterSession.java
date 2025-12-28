package client.controller.network.service;

import client.exception.GameCommunicationException;
import messagesbase.UniquePlayerIdentifier;

import java.util.Objects;
import java.util.Optional;

/**
 * Holds NetworkCenter session state (currently: registered player id).
 */
final class NetworkCenterSession {

    private static final int HTTP_STATUS_UNKNOWN = -1;

    private Optional<UniquePlayerIdentifier> playerId = Optional.empty();

    Optional<UniquePlayerIdentifier> playerId() {
        return playerId;
    }

    void setPlayerId(UniquePlayerIdentifier playerId) {
        this.playerId = Optional.of(Objects.requireNonNull(playerId, "playerId is required"));
    }

    UniquePlayerIdentifier requirePlayerId(String serverBaseUrl, String operation, String message)
            throws GameCommunicationException {
        return requirePlayerId(serverBaseUrl, operation, message, HTTP_STATUS_UNKNOWN);
    }

    UniquePlayerIdentifier requirePlayerId(String serverBaseUrl, String operation, String message, int httpStatusCode)
            throws GameCommunicationException {
        if (playerId.isEmpty()) {
            throw new GameCommunicationException(message, serverBaseUrl, operation, httpStatusCode);
        }
        return playerId.get();
    }
}
