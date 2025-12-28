package client.controller.network.service;

import client.model.Direction;
import client.model.mapper.MapNode;
import client.model.mapper.PlayerHalfMap;
import messagesbase.UniquePlayerIdentifier;

import java.util.Collection;
import java.util.List;

/**
 * Backwards-compatible facade for conversion logic.
 *
 * <p>New code should prefer {@link ClientToServerConverter} and {@link ServerToClientConverter}
 * directly. This class remains to keep existing callers functional.
 */
class Converter {

    private final ClientToServerConverter clientToServer = new ClientToServerConverter();
    private final ServerToClientConverter serverToClient = new ServerToClientConverter();

    public client.model.GameState convertServerGamestate(messagesbase.messagesfromserver.GameState serverGameState, UniquePlayerIdentifier playerId) {
        return serverToClient.convertServerGamestate(serverGameState, playerId);
    }

    public client.model.PlayerState convertServerPlayerState(messagesbase.messagesfromserver.PlayerState serverPlayerState, MapNode playerMapNode) {
        return serverToClient.convertServerPlayerState(serverPlayerState, playerMapNode);
    }

    public client.model.PlayerStatus convertServerStatus(messagesbase.messagesfromserver.EPlayerGameState serverStatus){
        return serverToClient.convertServerStatus(serverStatus);
    }

    public client.model.mapper.GameMap convertServerMap(messagesbase.messagesfromserver.FullMap serverMap) {
        return serverToClient.convertServerMap(serverMap);
    }

    public MapNode convertServerMapNode(messagesbase.messagesfromserver.FullMapNode serverMapNode) {
        return serverToClient.convertServerMapNode(serverMapNode);
    }

    public boolean serverMapHasEnemyFort(messagesbase.messagesfromserver.FullMap serverMap) {
        return serverToClient.serverMapHasEnemyFort(serverMap);
    }

    public messagesbase.messagesfromclient.PlayerHalfMap convertClientHalfMap(PlayerHalfMap halfMap, UniquePlayerIdentifier playerId) {
        return clientToServer.convertClientHalfMap(halfMap, playerId);
    }

    public Collection<messagesbase.messagesfromclient.PlayerHalfMapNode> convertClientNodes(List<MapNode> nodes) {
        return clientToServer.convertClientNodes(nodes);
    }

    public messagesbase.messagesfromclient.ETerrain convertClientTerrain(client.model.mapper.Terrain clientTerrain) {
        return clientToServer.convertClientTerrain(clientTerrain);
    }

    public client.model.mapper.Terrain convertServerTerrain(messagesbase.messagesfromclient.ETerrain serverTerrain) {
        return serverToClient.convertServerTerrain(serverTerrain);
    }

    public messagesbase.messagesfromclient.EMove convertClientDirection(Direction d){
        return clientToServer.convertClientDirection(d);
    }

    // helpers
    
    public boolean isFortOnServerNode(messagesbase.messagesfromserver.FullMapNode node) {
        return serverToClient.isFortOnServerNode(node);
    }

    public boolean isTreasureOnServerNode(messagesbase.messagesfromserver.FullMapNode node) {
        return serverToClient.isTreasureOnServerNode(node);
    }


}
