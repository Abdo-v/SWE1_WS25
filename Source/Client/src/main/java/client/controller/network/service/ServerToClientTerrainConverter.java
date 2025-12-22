package client.controller.network.service;

import client.model.mapper.Terrain;
import messagesbase.messagesfromclient.ETerrain;

/**
 * Converts network (messagesbase) terrain to client/internal terrain.
 */
public class ServerToClientTerrainConverter {

    public Terrain convert(ETerrain serverTerrain) {
        if (serverTerrain == ETerrain.Mountain) {
            return Terrain.MOUNTAIN;
        } else if (serverTerrain == ETerrain.Water) {
            return Terrain.WATER;
        } else {
            return Terrain.GRASS;
        }
    }
}
