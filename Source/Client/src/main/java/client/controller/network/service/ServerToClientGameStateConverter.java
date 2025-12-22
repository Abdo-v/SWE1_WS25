package client.controller.network.service;

import client.model.GameState;
import client.model.PlayerState;
import client.model.mapper.MapNode;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromserver.EPlayerPositionState;

import java.util.ArrayList;

/**
 * Converts a server game state to the client/internal game state.
 */
public class ServerToClientGameStateConverter {

    private final ServerToClientPlayerStateConverter playerStateConverter;
    private final ServerToClientMapConverter mapConverter;
    private final ServerToClientTerrainConverter terrainConverter;
    private final ServerToClientFullMapQueries mapQueries;

    public ServerToClientGameStateConverter(
            ServerToClientPlayerStateConverter playerStateConverter,
            ServerToClientMapConverter mapConverter,
            ServerToClientTerrainConverter terrainConverter,
            ServerToClientFullMapQueries mapQueries
    ) {
        this.playerStateConverter = playerStateConverter;
        this.mapConverter = mapConverter;
        this.terrainConverter = terrainConverter;
        this.mapQueries = mapQueries;
    }

    public GameState convert(messagesbase.messagesfromserver.GameState serverGameState, UniquePlayerIdentifier playerId) {
        ArrayList<PlayerState> clientPlayers = new ArrayList<>();
        messagesbase.messagesfromserver.FullMap serverMap = serverGameState.getMap();

        if (serverGameState.getPlayers() != null && !serverGameState.getPlayers().isEmpty()) {
            String myPlayerUniqueId = null;
            if (playerId != null) {
                myPlayerUniqueId = playerId.getUniquePlayerID();
            }
            if (myPlayerUniqueId != null) {
                for (messagesbase.messagesfromserver.PlayerState serverPlayer : serverGameState.getPlayers()) {
                    if (serverPlayer.getUniquePlayerID().equals(myPlayerUniqueId)) {
                        MapNode playerMapNode = new MapNode();
                        for (messagesbase.messagesfromserver.FullMapNode node : serverMap.getMapNodes()) {
                            if (node.getPlayerPositionState() == EPlayerPositionState.MyPlayerPosition
                                    || node.getPlayerPositionState() == EPlayerPositionState.BothPlayerPosition) {
                                playerMapNode.setX(node.getX());
                                playerMapNode.setY(node.getY());
                                playerMapNode.setTerrain(terrainConverter.convert(node.getTerrain()));
                                playerMapNode.setFortPresent(mapQueries.isFortOnServerNode(node));
                            }
                        }
                        clientPlayers.add(playerStateConverter.convert(serverPlayer, playerMapNode));
                        break;
                    }
                }
            }
            for (messagesbase.messagesfromserver.PlayerState serverPlayer : serverGameState.getPlayers()) {
                if (myPlayerUniqueId == null || !serverPlayer.getUniquePlayerID().equals(myPlayerUniqueId)) {
                    MapNode playerMapNode = new MapNode();
                    for (messagesbase.messagesfromserver.FullMapNode node : serverMap.getMapNodes()) {
                        if (node.getPlayerPositionState() == EPlayerPositionState.EnemyPlayerPosition
                                || node.getPlayerPositionState() == EPlayerPositionState.BothPlayerPosition) {
                            playerMapNode.setX(node.getX());
                            playerMapNode.setY(node.getY());
                            playerMapNode.setTerrain(terrainConverter.convert(node.getTerrain()));
                            playerMapNode.setFortPresent(mapQueries.isFortOnServerNode(node));
                        }
                    }
                    clientPlayers.add(playerStateConverter.convert(serverPlayer, playerMapNode));
                }
            }
        }

        GameState gameState = new GameState(
                serverGameState.getGameStateId(),
                clientPlayers,
                mapConverter.convert(serverGameState.getMap())
        );

        gameState.setTreasureCollected(clientPlayers.get(0).hasCollectedTreasure());
        gameState.setOpponentFortFound(mapQueries.serverMapHasEnemyFort(serverGameState.getMap()));

        if (mapQueries.getTreasurePositionFromServerMap(serverGameState.getMap()) != null) {
            gameState.setTreasurePosition(mapQueries.getTreasurePositionFromServerMap(serverGameState.getMap()));
        } else {
            gameState.setTreasurePosition(null);
        }

        if (mapQueries.getEnemyFortMapNodeFromServerMap(serverGameState.getMap()) != null) {
            gameState.setOpponentFortPosition(mapQueries.getEnemyFortMapNodeFromServerMap(serverGameState.getMap()));
        } else {
            gameState.setOpponentFortPosition(null);
        }

        return gameState;
    }
}
