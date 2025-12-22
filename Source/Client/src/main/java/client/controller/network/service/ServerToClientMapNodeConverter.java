package client.controller.network.service;

import client.model.mapper.MapNode;
import messagesbase.messagesfromserver.FullMapNode;

/**
 * Converts a server full-map node to the client/internal map node.
 */
public class ServerToClientMapNodeConverter {

    private final ServerToClientTerrainConverter terrainConverter;
    private final ServerToClientFullMapQueries nodeQueries;

    public ServerToClientMapNodeConverter(ServerToClientTerrainConverter terrainConverter, ServerToClientFullMapQueries nodeQueries) {
        this.terrainConverter = terrainConverter;
        this.nodeQueries = nodeQueries;
    }

    public MapNode convert(FullMapNode serverMapNode) {
        return new MapNode(
                serverMapNode.getX(),
                serverMapNode.getY(),
                terrainConverter.convert(serverMapNode.getTerrain()),
                nodeQueries.isFortOnServerNode(serverMapNode),
                nodeQueries.isTreasureOnServerNode(serverMapNode)
        );
    }
}
