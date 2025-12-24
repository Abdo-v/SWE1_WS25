package client.controller.network.service;

import client.model.Direction;
import client.model.mapper.MapNode;
import client.model.mapper.PlayerHalfMap;
import messagesbase.UniquePlayerIdentifier;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Converts client/internal model types to network (messagesbase) types.
 */
class ClientToServerConverter {

    private final ClientToServerTerrainConverter terrainConverter = new ClientToServerTerrainConverter();
    private final ClientToServerHalfMapNodeConverter halfMapNodeConverter = new ClientToServerHalfMapNodeConverter(terrainConverter);
    private final ClientToServerHalfMapConverter halfMapConverter = new ClientToServerHalfMapConverter(halfMapNodeConverter);
    private final ClientToServerMoveConverter moveConverter = new ClientToServerMoveConverter();

    public messagesbase.messagesfromclient.PlayerHalfMap convertClientHalfMap(PlayerHalfMap halfMap, UniquePlayerIdentifier playerId) {
        return halfMapConverter.convert(halfMap, playerId);
    }

    public Collection<messagesbase.messagesfromclient.PlayerHalfMapNode> convertClientNodes(List<MapNode> nodes) {
        return halfMapNodeConverter.convert(nodes);
    }

    public messagesbase.messagesfromclient.ETerrain convertClientTerrain(client.model.mapper.Terrain clientTerrain) {
        return terrainConverter.convert(clientTerrain);
    }

    public messagesbase.messagesfromclient.EMove convertClientDirection(Direction d) {
        return moveConverter.convert(d);
    }
}
