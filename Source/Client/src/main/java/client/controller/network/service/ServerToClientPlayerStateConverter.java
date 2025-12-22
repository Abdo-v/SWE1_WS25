package client.controller.network.service;

import client.model.PlayerState;
import client.model.PlayerStatus;
import client.model.mapper.MapNode;

/**
 * Converts a server player state to the client/internal player state.
 */
public class ServerToClientPlayerStateConverter {

    private final ServerToClientStatusConverter statusConverter;

    public ServerToClientPlayerStateConverter(ServerToClientStatusConverter statusConverter) {
        this.statusConverter = statusConverter;
    }

    public PlayerState convert(messagesbase.messagesfromserver.PlayerState serverPlayerState, MapNode playerMapNode) {
        PlayerStatus status = statusConverter.convert(serverPlayerState.getState());
        return new PlayerState(
                serverPlayerState.getUniquePlayerID(),
                serverPlayerState.getFirstName(),
                serverPlayerState.getLastName(),
                serverPlayerState.getUAccount(),
                serverPlayerState.hasCollectedTreasure(),
                playerMapNode,
                status
        );
    }
}
