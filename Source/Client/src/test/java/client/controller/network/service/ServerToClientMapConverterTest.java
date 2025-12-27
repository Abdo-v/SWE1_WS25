package client.controller.network.service;

import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.OwnToOppMapOrientation;
import client.model.mapper.Terrain;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromserver.EFortState;
import messagesbase.messagesfromserver.EPlayerPositionState;
import messagesbase.messagesfromserver.ETreasureState;
import messagesbase.messagesfromserver.FullMap;
import messagesbase.messagesfromserver.FullMapNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ServerToClientMapConverter}.
 *
 * <p>Validates orientation detection and correct conversion of map nodes.
 */
class ServerToClientMapConverterTest {

    /**
     * Ensures an empty server map yields an empty internal {@link GameMap}.
     */
    @Test
    void convert_whenServerMapIsEmpty_returnsEmptyGameMap() {
        ServerToClientMapNodeConverter mapNodeConverter = mock(ServerToClientMapNodeConverter.class);
        ServerToClientMapConverter converter = new ServerToClientMapConverter(mapNodeConverter);

        GameMap result = converter.convert(new FullMap());

        assertEquals(0, result.getContentSize());
        assertThrows(IllegalStateException.class, result::getOrientation);
    }

    /**
     * Ensures vertical map orientation is detected as {@link OwnToOppMapOrientation#UP_DOWN} when the own fort is in the top half.
     */
    @Test
    void convert_whenVerticalAndFortInTopHalf_resolvesUpDown() {
        ServerToClientMapNodeConverter mapNodeConverter = mock(ServerToClientMapNodeConverter.class);

        FullMapNode fortNode = new FullMapNode(
                ETerrain.Grass,
                EPlayerPositionState.NoPlayerPresent,
                ETreasureState.NoOrUnknownTreasureState,
                EFortState.MyFortPresent,
                0,
                0
        );
        FullMapNode otherHalfNode = new FullMapNode(
                ETerrain.Grass,
                EPlayerPositionState.NoPlayerPresent,
                ETreasureState.NoOrUnknownTreasureState,
                EFortState.NoOrUnknownFortState,
                0,
                5
        );

        when(mapNodeConverter.convert(fortNode)).thenReturn(new MapNode(0, 0, Terrain.GRASS, true, false));
        when(mapNodeConverter.convert(otherHalfNode)).thenReturn(new MapNode(0, 5, Terrain.GRASS, false, false));

        ServerToClientMapConverter converter = new ServerToClientMapConverter(mapNodeConverter);
        GameMap result = converter.convert(new FullMap(List.of(fortNode, otherHalfNode)));

        assertEquals(OwnToOppMapOrientation.UP_DOWN, result.getOrientation());
        assertEquals(9, result.getMaxX());
        assertEquals(9, result.getMaxY());
        assertEquals(2, result.getContentSize());
        verify(mapNodeConverter, times(1)).convert(fortNode);
        verify(mapNodeConverter, times(1)).convert(otherHalfNode);
    }

    /**
     * Ensures horizontal map orientation is detected as {@link OwnToOppMapOrientation#LEFT_RIGHT} when the own fort is in the left half.
     */
    @Test
    void convert_whenHorizontalAndFortInLeftHalf_resolvesLeftRight() {
        ServerToClientMapNodeConverter mapNodeConverter = mock(ServerToClientMapNodeConverter.class);

        FullMapNode fortNode = new FullMapNode(
                ETerrain.Grass,
                EPlayerPositionState.NoPlayerPresent,
                ETreasureState.NoOrUnknownTreasureState,
                EFortState.MyFortPresent,
                0,
                0
        );
        FullMapNode rightSideNode = new FullMapNode(
                ETerrain.Grass,
                EPlayerPositionState.NoPlayerPresent,
                ETreasureState.NoOrUnknownTreasureState,
                EFortState.NoOrUnknownFortState,
                10,
                0
        );

        when(mapNodeConverter.convert(fortNode)).thenReturn(new MapNode(0, 0, Terrain.GRASS, true, false));
        when(mapNodeConverter.convert(rightSideNode)).thenReturn(new MapNode(10, 0, Terrain.GRASS, false, false));

        ServerToClientMapConverter converter = new ServerToClientMapConverter(mapNodeConverter);
        GameMap result = converter.convert(new FullMap(List.of(fortNode, rightSideNode)));

        assertEquals(OwnToOppMapOrientation.LEFT_RIGHT, result.getOrientation());
        assertEquals(19, result.getMaxX());
        assertEquals(4, result.getMaxY());
        assertEquals(2, result.getContentSize());
        verify(mapNodeConverter, times(1)).convert(fortNode);
        verify(mapNodeConverter, times(1)).convert(rightSideNode);
    }
}
