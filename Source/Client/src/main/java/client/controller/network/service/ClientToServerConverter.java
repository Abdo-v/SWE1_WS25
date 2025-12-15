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
public class ClientToServerConverter {

    public messagesbase.messagesfromclient.PlayerHalfMap convertClientHalfMap(PlayerHalfMap halfMap, UniquePlayerIdentifier playerId) {
        messagesbase.messagesfromclient.PlayerHalfMap serverHalfMap =
                new messagesbase.messagesfromclient.PlayerHalfMap(playerId, convertClientNodes(halfMap.getMapNodes()));
        return serverHalfMap;
    }

    public Collection<messagesbase.messagesfromclient.PlayerHalfMapNode> convertClientNodes(List<MapNode> nodes) {
        HashSet<messagesbase.messagesfromclient.PlayerHalfMapNode> serverNodes = new HashSet<>();
        for (MapNode node : nodes) {
            serverNodes.add(new messagesbase.messagesfromclient.PlayerHalfMapNode(
                    node.getX(),
                    node.getY(),
                    node.isFortPresent(),
                    convertClientTerrain(node.getTerrain())
            ));
        }
        return serverNodes;
    }

    public messagesbase.messagesfromclient.ETerrain convertClientTerrain(client.model.mapper.Terrain clientTerrain) {
        if (clientTerrain == client.model.mapper.Terrain.MOUNTAIN) {
            return messagesbase.messagesfromclient.ETerrain.Mountain;
        } else if (clientTerrain == client.model.mapper.Terrain.WATER) {
            return messagesbase.messagesfromclient.ETerrain.Water;
        } else {
            return messagesbase.messagesfromclient.ETerrain.Grass;
        }
    }

    public messagesbase.messagesfromclient.EMove convertClientDirection(Direction d) {
        if (d == Direction.UP) {
            return messagesbase.messagesfromclient.EMove.Up;
        } else if (d == Direction.DOWN) {
            return messagesbase.messagesfromclient.EMove.Down;
        } else if (d == Direction.LEFT) {
            return messagesbase.messagesfromclient.EMove.Left;
        } else if (d == Direction.RIGHT) {
            return messagesbase.messagesfromclient.EMove.Right;
        } else {
            return null;
        }
    }
}
