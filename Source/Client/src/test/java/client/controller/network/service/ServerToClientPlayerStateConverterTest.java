package client.controller.network.service;

import client.model.PlayerState;
import client.model.PlayerStatus;
import client.model.mapper.MapNode;
import client.model.mapper.Terrain;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromserver.EPlayerGameState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ServerToClientPlayerStateConverter}.
 *
 * <p>Focuses on correct field mapping and propagation of the current position map node.
 */
class ServerToClientPlayerStateConverterTest {

    /**
        * Converts a server player state and verifies all relevant fields are mapped.
     */
    @Test
    void convert_mapsAllFields() {
        ServerToClientStatusConverter statusConverter = new ServerToClientStatusConverter();
        ServerToClientPlayerStateConverter converter = new ServerToClientPlayerStateConverter(statusConverter);

        UniquePlayerIdentifier pid = UniquePlayerIdentifier.of("p-1");
        messagesbase.messagesfromserver.PlayerState server = new messagesbase.messagesfromserver.PlayerState(
                "Alice",
                "Example",
                "u123",
                EPlayerGameState.MustAct,
                pid,
                true
        );

        MapNode position = new MapNode(3, 2, Terrain.GRASS, false, false);

        PlayerState actual = converter.convert(server, position);

        assertEquals("p-1", actual.getPlayerID());
        assertEquals("Alice", actual.getFirstName());
        assertEquals("Example", actual.getLastName());
        assertEquals("u123", actual.getUAccount());
        assertEquals(PlayerStatus.MUST_ACT, actual.getStatus());
        assertTrue(actual.hasCollectedTreasure());
        assertEquals(position, actual.getCurrentPosition().orElseThrow());
    }
}
