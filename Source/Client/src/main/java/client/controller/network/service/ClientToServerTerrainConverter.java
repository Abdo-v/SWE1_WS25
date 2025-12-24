package client.controller.network.service;

import client.model.mapper.Terrain;
import messagesbase.messagesfromclient.ETerrain;

/**
 * Converts client/internal terrain to network (messagesbase) terrain.
 */
class ClientToServerTerrainConverter {

    public ETerrain convert(Terrain clientTerrain) {
        if (clientTerrain == Terrain.MOUNTAIN) {
            return ETerrain.Mountain;
        } else if (clientTerrain == Terrain.WATER) {
            return ETerrain.Water;
        } else {
            return ETerrain.Grass;
        }
    }
}
