package client.controller.network.service;

import client.model.mapper.MapNode;
import client.model.mapper.Terrain;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromserver.EFortState;
import messagesbase.messagesfromserver.EPlayerPositionState;
import messagesbase.messagesfromserver.ETreasureState;
import messagesbase.messagesfromserver.FullMapNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ServerToClientMapNodeConverter}.
 */
class ServerToClientMapNodeConverterTest {

    /**
     * Ensures a single server {@link FullMapNode} is mapped to a {@link MapNode} with the expected fields.
     */
    @Test
    void convert_mapsCoordinatesTerrainFortAndTreasure() {
        ServerToClientTerrainConverter terrainConverter = mock(ServerToClientTerrainConverter.class);
        ServerToClientFullMapQueries nodeQueries = mock(ServerToClientFullMapQueries.class);

        when(terrainConverter.convert(ETerrain.Water)).thenReturn(Terrain.WATER);

        FullMapNode serverNode = new FullMapNode(
                ETerrain.Water,
                EPlayerPositionState.NoPlayerPresent,
                ETreasureState.MyTreasureIsPresent,
                EFortState.MyFortPresent,
                2,
                3
        );

        when(nodeQueries.isFortOnServerNode(serverNode)).thenReturn(true);
        when(nodeQueries.isTreasureOnServerNode(serverNode)).thenReturn(true);

        ServerToClientMapNodeConverter converter = new ServerToClientMapNodeConverter(terrainConverter, nodeQueries);

        MapNode mapped = converter.convert(serverNode);

        assertEquals(2, mapped.getX());
        assertEquals(3, mapped.getY());
        assertEquals(Terrain.WATER, mapped.getTerrain());
        assertEquals(true, mapped.isFortPresent());
        assertEquals(true, mapped.isTreasurePresent());
    }
}
