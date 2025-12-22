package client.controller.network.service;

import client.model.GameState;
import client.model.PlayerState;
import client.model.mapper.MapNode;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromserver.EPlayerPositionState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

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

        Collection<messagesbase.messagesfromserver.PlayerState> serverPlayers =
                Optional.ofNullable(serverGameState.getPlayers()).orElse(Collections.emptySet());

        Optional<String> myPlayerUniqueId = Optional.ofNullable(playerId).map(UniquePlayerIdentifier::getUniquePlayerID);

        if (!serverPlayers.isEmpty()) {
            if (myPlayerUniqueId.isPresent()) {
                String myId = myPlayerUniqueId.get();
                for (messagesbase.messagesfromserver.PlayerState serverPlayer : serverPlayers) {
                    if (serverPlayer.getUniquePlayerID().equals(myId)) {
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

            for (messagesbase.messagesfromserver.PlayerState serverPlayer : serverPlayers) {
                if (myPlayerUniqueId.isEmpty() || !serverPlayer.getUniquePlayerID().equals(myPlayerUniqueId.get())) {
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

        mapQueries.getTreasurePositionFromServerMap(serverGameState.getMap())
                .ifPresent(gameState::setTreasurePosition);

        mapQueries.getEnemyFortMapNodeFromServerMap(serverGameState.getMap())
                .ifPresent(gameState::setOpponentFortPosition);

        return gameState;
    }
}
