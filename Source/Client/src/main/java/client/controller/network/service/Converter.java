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
public class Converter {

    private final ClientToServerConverter clientToServer = new ClientToServerConverter();
    private final ServerToClientConverter serverToClient = new ServerToClientConverter();

    Converter() {
    }

    /**
     * Converts the server game state to the internal representation.
     * @param serverGameState The server game state.
     * @return The internal game state.
     */
    public client.model.GameState convertServerGamestate(messagesbase.messagesfromserver.GameState serverGameState, UniquePlayerIdentifier playerId) {
        return serverToClient.convertServerGamestate(serverGameState, playerId);
    }

    /**
     * Converts the server player state to the internal representation.
     * @param serverPlayerState The server player state.
     * @param playerMapNode The player's map node.
     * @return The internal player state.
     */
    public client.model.PlayerState convertServerPlayerState(messagesbase.messagesfromserver.PlayerState serverPlayerState, MapNode playerMapNode) {
        return serverToClient.convertServerPlayerState(serverPlayerState, playerMapNode);
    }

    public client.model.PlayerStatus convertServerStatus(messagesbase.messagesfromserver.EPlayerGameState serverStatus){
        return serverToClient.convertServerStatus(serverStatus);
    }

    /**
     * Converts the server map to the internal game map.
     * @param serverMap The server map.
     * @return The internal game map.
     */
    public client.model.mapper.GameMap convertServerMap(messagesbase.messagesfromserver.FullMap serverMap) {
        return serverToClient.convertServerMap(serverMap);
    }

    /**
     * Converts the server map node to the internal map node.
     * @param serverMapNode The server map node.
     * @return The internal map node.
     */
    public MapNode convertServerMapNode(messagesbase.messagesfromserver.FullMapNode serverMapNode) {
        return serverToClient.convertServerMapNode(serverMapNode);
    }

    /**
     * Checks if the server map has an enemy fort.
     * @param serverMap The server map.
     * @return True if enemy fort is present, false otherwise.
     */
    public boolean serverMapHasEnemyFort(messagesbase.messagesfromserver.FullMap serverMap) {
        return serverToClient.serverMapHasEnemyFort(serverMap);
    }

    /**
     * Gets the treasure position from the server map.
     * @param serverMap The server map.
        * @return The treasure position as a MapNode, if available.
     */
    /**
     * Converts the internal half map to the Server message format.
     * @param halfMap Our internal half map representation.
     * @return A half map in the format expected by the server.
     */
    public messagesbase.messagesfromclient.PlayerHalfMap convertClientHalfMap(PlayerHalfMap halfMap, UniquePlayerIdentifier playerId) {
        return clientToServer.convertClientHalfMap(halfMap, playerId);
    }

    /**
     * Converts a list of internal map nodes to Server map nodes.
     * @param nodes List of internal map nodes.
     * @return Collection of Server map nodes.
     */
    public Collection<messagesbase.messagesfromclient.PlayerHalfMapNode> convertClientNodes(List<MapNode> nodes) {
        return clientToServer.convertClientNodes(nodes);
    }

    /**
     * Converts client terrain to Server terrain.
     * @param clientTerrain The client terrain.
     * @return The corresponding Server terrain.
     */
    public messagesbase.messagesfromclient.ETerrain convertClientTerrain(client.model.mapper.Terrain clientTerrain) {
        return clientToServer.convertClientTerrain(clientTerrain);
    }

    /**
     * Converts server terrain to client terrain.
     * @param serverTerrain The server terrain.
     * @return The corresponding client terrain.
     */
    public client.model.mapper.Terrain convertServerTerrain(messagesbase.messagesfromclient.ETerrain serverTerrain) {
        return serverToClient.convertServerTerrain(serverTerrain);
    }

    /**
     * Converts the client direction to the Server move.
     * @param d The client direction.
     * @return The Server move.
     */
    public messagesbase.messagesfromclient.EMove convertClientDirection(Direction d){
        return clientToServer.convertClientDirection(d);
    }

    // helpers
    
    /**
     * Checks if a fort is present on the given Server node.
     * @param node The Server node.
     * @return True if a fort is present, false otherwise.
     */
    public boolean isFortOnServerNode(messagesbase.messagesfromserver.FullMapNode node) {
        return serverToClient.isFortOnServerNode(node);
    }

    public boolean isTreasureOnServerNode(messagesbase.messagesfromserver.FullMapNode node) {
        return serverToClient.isTreasureOnServerNode(node);
    }


}
