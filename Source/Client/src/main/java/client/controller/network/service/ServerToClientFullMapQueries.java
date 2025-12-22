package client.controller.network.service;

import client.model.mapper.MapNode;
import messagesbase.messagesfromserver.EFortState;
import messagesbase.messagesfromserver.ETreasureState;
import messagesbase.messagesfromserver.FullMap;
import messagesbase.messagesfromserver.FullMapNode;

/**
 * Queries and extracts information from server full maps/nodes.
 */
public class ServerToClientFullMapQueries {

    private final ServerToClientTerrainConverter terrainConverter;

    public ServerToClientFullMapQueries(ServerToClientTerrainConverter terrainConverter) {
        this.terrainConverter = terrainConverter;
    }

    public boolean isFortOnServerNode(FullMapNode node) {
        return node.getFortState() == EFortState.MyFortPresent
                || node.getFortState() == EFortState.EnemyFortPresent;
    }

    public boolean isTreasureOnServerNode(FullMapNode node) {
        return node.getTreasureState() == ETreasureState.MyTreasureIsPresent;
    }

    public boolean serverMapHasEnemyFort(FullMap serverMap) {
        for (FullMapNode node : serverMap.getMapNodes()) {
            if (node.getFortState() == EFortState.EnemyFortPresent) {
                return true;
            }
        }
        return false;
    }

    public MapNode getTreasurePositionFromServerMap(FullMap serverMap) {
        for (FullMapNode node : serverMap.getMapNodes()) {
            if (node.getTreasureState() == ETreasureState.MyTreasureIsPresent) {
                return new MapNode(node.getX(), node.getY(), terrainConverter.convert(node.getTerrain()), isFortOnServerNode(node), true);
            }
        }
        return null;
    }

    public MapNode getEnemyFortMapNodeFromServerMap(FullMap serverMap) {
        for (FullMapNode node : serverMap.getMapNodes()) {
            if (node.getFortState() == EFortState.EnemyFortPresent) {
                return new MapNode(node.getX(), node.getY(), terrainConverter.convert(node.getTerrain()), true, false);
            }
        }
        return null;
    }
}
