package client.controller.network.service;

import client.model.GameState;
import client.model.PlayerState;
import client.model.PlayerStatus;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.Terrain;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromserver.EFortState;
import messagesbase.messagesfromserver.EPlayerGameState;
import messagesbase.messagesfromserver.EPlayerPositionState;
import messagesbase.messagesfromserver.ETreasureState;
import messagesbase.messagesfromserver.FullMap;
import messagesbase.messagesfromserver.FullMapNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ServerToClientGameStateConverter}.
 *
 * <p>Uses mocks to isolate conversion logic from network polling and other infrastructure.
 */
class ServerToClientGameStateConverterTest {

    /**
     * Converts a server game state containing the current player and verifies key derived flags are applied.
     */
    @Test
    void convert_mapsCurrentPlayerAndDerivedFlags() {
        ServerToClientPlayerStateConverter playerStateConverter = mock(ServerToClientPlayerStateConverter.class);
        ServerToClientMapConverter mapConverter = mock(ServerToClientMapConverter.class);
        ServerToClientTerrainConverter terrainConverter = mock(ServerToClientTerrainConverter.class);
        ServerToClientFullMapQueries mapQueries = mock(ServerToClientFullMapQueries.class);

        UniquePlayerIdentifier myPlayerId = UniquePlayerIdentifier.of("me");

        messagesbase.messagesfromserver.PlayerState serverMe = new messagesbase.messagesfromserver.PlayerState(
                "Me",
                "Player",
                "u-me",
                EPlayerGameState.MustWait,
                myPlayerId,
                true
        );

        FullMapNode myPosNode = new FullMapNode(
                ETerrain.Grass,
                EPlayerPositionState.MyPlayerPosition,
                ETreasureState.NoOrUnknownTreasureState,
                EFortState.NoOrUnknownFortState,
                1,
                2
        );

        FullMap serverMap = new FullMap(List.of(myPosNode));
        messagesbase.messagesfromserver.GameState serverGameState = new messagesbase.messagesfromserver.GameState(
                serverMap,
                List.of(serverMe),
                "gs-1"
        );

        when(terrainConverter.convert(ETerrain.Grass)).thenReturn(Terrain.GRASS);
        when(mapQueries.isFortOnServerNode(myPosNode)).thenReturn(false);

        PlayerState clientMe = new PlayerState("me", "Me", "Player", "u-me", true, new MapNode(1, 2, Terrain.GRASS, false, false), PlayerStatus.MUST_WAIT);
        when(playerStateConverter.convert(any(messagesbase.messagesfromserver.PlayerState.class), any(MapNode.class)))
                .thenReturn(clientMe);

        when(mapConverter.convert(serverMap)).thenReturn(new GameMap());
        when(mapQueries.serverMapHasEnemyFort(serverMap)).thenReturn(true);
        when(mapQueries.getTreasurePositionFromServerMap(serverMap)).thenReturn(Optional.empty());
        when(mapQueries.getEnemyFortMapNodeFromServerMap(serverMap)).thenReturn(Optional.empty());

        ServerToClientGameStateConverter converter = new ServerToClientGameStateConverter(
                playerStateConverter,
                mapConverter,
                terrainConverter,
                mapQueries
        );

        GameState result = converter.convert(serverGameState, myPlayerId);

        assertEquals("gs-1", result.getGameStateID());
        assertTrue(result.isTreasureCollected());
        assertTrue(result.isOpponentFortFound());
        assertEquals("me", result.getCurrentPlayerState().orElseThrow().getPlayerID());

        verify(mapConverter).convert(serverMap);
        verify(mapQueries).serverMapHasEnemyFort(serverMap);
    }
}
