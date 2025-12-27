package client.controller.network.service;

import client.model.mapper.GameMap;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Additional unit tests for {@link ServerToClientMapConverter} focusing on orientation fallback behavior.
 */
class ServerToClientMapConverterOrientationFallbackTest {

    /**
     * Ensures that when no full-map orientation can be inferred, the converter returns an empty internal map.
     */
    @Test
    void convert_whenOrientationCannotBeResolved_returnsEmptyGameMap() {
        ServerToClientMapNodeConverter mapNodeConverter = mock(ServerToClientMapNodeConverter.class);

        FullMapNode node = new FullMapNode(
                ETerrain.Grass,
                EPlayerPositionState.NoPlayerPresent,
                ETreasureState.NoOrUnknownTreasureState,
                EFortState.NoOrUnknownFortState,
                0,
                0
        );
        when(mapNodeConverter.convert(node)).thenReturn(new MapNode(0, 0, Terrain.GRASS, false, false));

        ServerToClientMapConverter converter = new ServerToClientMapConverter(mapNodeConverter);
        GameMap result = converter.convert(new FullMap(List.of(node)));

        assertEquals(0, result.getContentSize());
        assertThrows(IllegalStateException.class, result::getOrientation);
        verify(mapNodeConverter, times(1)).convert(node);
    }
}
