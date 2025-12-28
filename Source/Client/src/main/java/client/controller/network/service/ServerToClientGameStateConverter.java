package client.controller.network.service;

import client.model.GameState;
import client.model.PlayerState;
import client.model.mapper.MapNode;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromserver.EPlayerPositionState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Converts a server game state to the client/internal game state.
 */
class ServerToClientGameStateConverter {

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

    /**
     * Converts the server snapshot into the client model.
     *
     * <p>Key behaviors:
     * <ul>
     *   <li>Attempts to place the local player's {@link MapNode} by scanning {@code FullMapNode} position state.</li>
     *   <li>Creates an enemy player entry similarly (when present).</li>
     *   <li>Derives convenience flags/positions (treasure collected, enemy fort found/position).</li>
     * </ul>
     *
     * <p>Preserves the previous "best effort" null-handling semantics.
     */
    public GameState convert(messagesbase.messagesfromserver.GameState serverGameState, UniquePlayerIdentifier playerId) {
        ArrayList<PlayerState> clientPlayers = new ArrayList<>();
        messagesbase.messagesfromserver.FullMap serverMap = serverGameState.getMap();

        Collection<messagesbase.messagesfromserver.PlayerState> serverPlayers;
        try {
            serverPlayers = Objects.requireNonNull(serverGameState.getPlayers());
        } catch (NullPointerException e) {
            serverPlayers = Collections.emptySet();
        }

        String myPlayerUniqueId = "";
        boolean hasMyPlayerUniqueId;
        try {
            myPlayerUniqueId = Objects.requireNonNull(playerId).getUniquePlayerID();
            hasMyPlayerUniqueId = true;
        } catch (NullPointerException e) {
            hasMyPlayerUniqueId = false;
        }

        if (!serverPlayers.isEmpty()) {
            if (hasMyPlayerUniqueId) {
                for (messagesbase.messagesfromserver.PlayerState serverPlayer : serverPlayers) {
                    if (serverPlayer.getUniquePlayerID().equals(myPlayerUniqueId)) {
                        MapNode playerMapNode = new MapNode();
                        // Find the node that represents the local player.
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
                if (!hasMyPlayerUniqueId || !serverPlayer.getUniquePlayerID().equals(myPlayerUniqueId)) {
                    MapNode playerMapNode = new MapNode();
                    // Find the node that represents the enemy player.
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

        // Derive flags and special positions from the server map.
        gameState.setTreasureCollected(clientPlayers.get(0).hasCollectedTreasure());
        gameState.setOpponentFortFound(mapQueries.serverMapHasEnemyFort(serverGameState.getMap()));

        mapQueries.getTreasurePositionFromServerMap(serverGameState.getMap())
                .ifPresent(gameState::setTreasurePosition);

        mapQueries.getEnemyFortMapNodeFromServerMap(serverGameState.getMap())
                .ifPresent(gameState::setOpponentFortPosition);

        return gameState;
    }
}
