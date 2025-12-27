package client.controller.network.service;

import client.model.mapper.MapNode;
import client.model.mapper.Terrain;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromserver.EFortState;
import messagesbase.messagesfromserver.EPlayerPositionState;
import messagesbase.messagesfromserver.ETreasureState;
import messagesbase.messagesfromserver.FullMap;
import messagesbase.messagesfromserver.FullMapNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ServerToClientFullMapQueries}.
 *
 * <p>These tests validate extraction logic for forts and treasures from server-side full maps.
 */
class ServerToClientFullMapQueriesTest {

    /**
     * Ensures {@link ServerToClientFullMapQueries#isFortOnServerNode(FullMapNode)} follows the enum contract.
     */
    @Test
    void isFortOnServerNode_recognizesKnownFortStates() {
        ServerToClientTerrainConverter terrainConverter = mock(ServerToClientTerrainConverter.class);
        ServerToClientFullMapQueries queries = new ServerToClientFullMapQueries(terrainConverter);

        FullMapNode noFort = new FullMapNode(
                ETerrain.Grass,
                EPlayerPositionState.NoPlayerPresent,
                ETreasureState.NoOrUnknownTreasureState,
                EFortState.NoOrUnknownFortState,
                0,
                0
        );
        FullMapNode myFort = new FullMapNode(
                ETerrain.Grass,
                EPlayerPositionState.NoPlayerPresent,
                ETreasureState.NoOrUnknownTreasureState,
                EFortState.MyFortPresent,
                1,
                0
        );
        FullMapNode enemyFort = new FullMapNode(
                ETerrain.Grass,
                EPlayerPositionState.NoPlayerPresent,
                ETreasureState.NoOrUnknownTreasureState,
                EFortState.EnemyFortPresent,
                2,
                0
        );

        assertFalse(queries.isFortOnServerNode(noFort));
        assertTrue(queries.isFortOnServerNode(myFort));
        assertTrue(queries.isFortOnServerNode(enemyFort));
    }

    /**
     * Ensures {@link ServerToClientFullMapQueries#serverMapHasEnemyFort(FullMap)} detects presence correctly.
     */
    @Test
    void serverMapHasEnemyFort_detectsEnemyFort() {
        ServerToClientTerrainConverter terrainConverter = mock(ServerToClientTerrainConverter.class);
        ServerToClientFullMapQueries queries = new ServerToClientFullMapQueries(terrainConverter);

        FullMapNode enemyFort = new FullMapNode(
                ETerrain.Grass,
                EPlayerPositionState.NoPlayerPresent,
                ETreasureState.NoOrUnknownTreasureState,
                EFortState.EnemyFortPresent,
                0,
                0
        );

        FullMap serverMap = new FullMap(List.of(enemyFort));

        assertTrue(queries.serverMapHasEnemyFort(serverMap));
    }

    /**
     * Ensures treasure position extraction returns a {@link MapNode} with expected coordinates and treasure flag.
     */
    @Test
    void getTreasurePositionFromServerMap_whenTreasurePresent_returnsMappedNode() {
        ServerToClientTerrainConverter terrainConverter = mock(ServerToClientTerrainConverter.class);
        when(terrainConverter.convert(ETerrain.Grass)).thenReturn(Terrain.GRASS);

        ServerToClientFullMapQueries queries = new ServerToClientFullMapQueries(terrainConverter);

        FullMapNode treasureNode = new FullMapNode(
                ETerrain.Grass,
                EPlayerPositionState.NoPlayerPresent,
                ETreasureState.MyTreasureIsPresent,
                EFortState.NoOrUnknownFortState,
                4,
                1
        );

        Optional<MapNode> result = queries.getTreasurePositionFromServerMap(new FullMap(List.of(treasureNode)));

        MapNode mapped = result.orElseThrow();
        assertEquals(4, mapped.getX());
        assertEquals(1, mapped.getY());
        assertTrue(mapped.isTreasurePresent());
        assertFalse(mapped.isFortPresent());
    }

    /**
     * Ensures enemy fort position extraction returns a {@link MapNode} with expected coordinates.
     */
    @Test
    void getEnemyFortMapNodeFromServerMap_whenEnemyFortPresent_returnsMappedNode() {
        ServerToClientTerrainConverter terrainConverter = mock(ServerToClientTerrainConverter.class);
        when(terrainConverter.convert(ETerrain.Mountain)).thenReturn(Terrain.MOUNTAIN);

        ServerToClientFullMapQueries queries = new ServerToClientFullMapQueries(terrainConverter);

        FullMapNode fortNode = new FullMapNode(
                ETerrain.Mountain,
                EPlayerPositionState.NoPlayerPresent,
                ETreasureState.NoOrUnknownTreasureState,
                EFortState.EnemyFortPresent,
                7,
                3
        );

        Optional<MapNode> result = queries.getEnemyFortMapNodeFromServerMap(new FullMap(List.of(fortNode)));

        MapNode mapped = result.orElseThrow();
        assertEquals(7, mapped.getX());
        assertEquals(3, mapped.getY());
        assertTrue(mapped.isFortPresent());
        assertFalse(mapped.isTreasurePresent());
        assertEquals(Terrain.MOUNTAIN, mapped.getTerrain());
    }
}
