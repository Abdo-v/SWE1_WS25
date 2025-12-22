package client.controller.network.service;

import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.OwnToOppMapOrientation;
import messagesbase.UniquePlayerIdentifier;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Converts network (messagesbase) types to client/internal model types.
 */
public class ServerToClientConverter {

    private final ServerToClientTerrainConverter terrainConverter = new ServerToClientTerrainConverter();
    private final ServerToClientStatusConverter statusConverter = new ServerToClientStatusConverter();
    private final ServerToClientFullMapQueries fullMapQueries = new ServerToClientFullMapQueries(terrainConverter);
    private final ServerToClientMapNodeConverter mapNodeConverter = new ServerToClientMapNodeConverter(terrainConverter, fullMapQueries);
    private final ServerToClientMapConverter mapConverter = new ServerToClientMapConverter(mapNodeConverter);
    private final ServerToClientPlayerStateConverter playerStateConverter = new ServerToClientPlayerStateConverter(statusConverter);
    private final ServerToClientGameStateConverter gameStateConverter = new ServerToClientGameStateConverter(
            playerStateConverter,
            mapConverter,
            terrainConverter,
            fullMapQueries
    );

    public client.model.GameState convertServerGamestate(messagesbase.messagesfromserver.GameState serverGameState, UniquePlayerIdentifier playerId) {
        return gameStateConverter.convert(serverGameState, playerId);
    }

    public client.model.PlayerState convertServerPlayerState(messagesbase.messagesfromserver.PlayerState serverPlayerState, MapNode playerMapNode) {
        return playerStateConverter.convert(serverPlayerState, playerMapNode);
    }

    public client.model.PlayerStatus convertServerStatus(messagesbase.messagesfromserver.EPlayerGameState serverStatus) {
        return statusConverter.convert(serverStatus);
    }

    public GameMap convertServerMap(messagesbase.messagesfromserver.FullMap serverMap) {
        return mapConverter.convert(serverMap);
    }

    public MapNode convertServerMapNode(messagesbase.messagesfromserver.FullMapNode serverMapNode) {
        return mapNodeConverter.convert(serverMapNode);
    }

    public boolean serverMapHasEnemyFort(messagesbase.messagesfromserver.FullMap serverMap) {
        return fullMapQueries.serverMapHasEnemyFort(serverMap);
    }

    public client.model.mapper.Terrain convertServerTerrain(messagesbase.messagesfromclient.ETerrain serverTerrain) {
        return terrainConverter.convert(serverTerrain);
    }

    public boolean isFortOnServerNode(messagesbase.messagesfromserver.FullMapNode node) {
        return fullMapQueries.isFortOnServerNode(node);
    }

    public boolean isTreasureOnServerNode(messagesbase.messagesfromserver.FullMapNode node) {
        return fullMapQueries.isTreasureOnServerNode(node);
    }
}
