package client.controller.network.service;

import client.model.mapper.Terrain;
import messagesbase.messagesfromclient.ETerrain;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link ServerToClientTerrainConverter}.
 *
 * <p>Verifies that network terrain values are mapped to the correct internal terrain.
 */
class ServerToClientTerrainConverterTest {

    /**
     * Ensures every {@link ETerrain} maps to the expected internal {@link Terrain}.
     *
     * @param serverTerrain The network terrain value.
     */
    @ParameterizedTest
    @EnumSource(ETerrain.class)
    void convert_mapsAllServerTerrains(ETerrain serverTerrain) {
        ServerToClientTerrainConverter converter = new ServerToClientTerrainConverter();

        Terrain actual = converter.convert(serverTerrain);

        Terrain expected = switch (serverTerrain) {
            case Mountain -> Terrain.MOUNTAIN;
            case Water -> Terrain.WATER;
            case Grass -> Terrain.GRASS;
        };
        assertEquals(expected, actual);
    }
}
