package client.controller.network.service;

import client.model.mapper.MapNode;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Converts internal map nodes to server half-map nodes.
 */
class ClientToServerHalfMapNodeConverter {

    private final ClientToServerTerrainConverter terrainConverter;

    public ClientToServerHalfMapNodeConverter(ClientToServerTerrainConverter terrainConverter) {
        this.terrainConverter = terrainConverter;
    }

    public Collection<messagesbase.messagesfromclient.PlayerHalfMapNode> convert(List<MapNode> nodes) {
        HashSet<messagesbase.messagesfromclient.PlayerHalfMapNode> serverNodes = new HashSet<>();
        for (MapNode node : nodes) {
            serverNodes.add(new messagesbase.messagesfromclient.PlayerHalfMapNode(
                    node.getX(),
                    node.getY(),
                    node.isFortPresent(),
                    terrainConverter.convert(node.getTerrain())
            ));
        }
        return serverNodes;
    }
}
