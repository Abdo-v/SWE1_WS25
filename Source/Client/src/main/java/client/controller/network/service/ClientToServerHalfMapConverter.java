package client.controller.network.service;

import client.model.mapper.PlayerHalfMap;
import messagesbase.UniquePlayerIdentifier;

/**
 * Converts the internal half map to the server message format.
 */
class ClientToServerHalfMapConverter {

    private final ClientToServerHalfMapNodeConverter nodeConverter;

    public ClientToServerHalfMapConverter(ClientToServerHalfMapNodeConverter nodeConverter) {
        this.nodeConverter = nodeConverter;
    }

    public messagesbase.messagesfromclient.PlayerHalfMap convert(PlayerHalfMap halfMap, UniquePlayerIdentifier playerId) {
        return new messagesbase.messagesfromclient.PlayerHalfMap(playerId, nodeConverter.convert(halfMap.getMapNodes()));
    }
}
