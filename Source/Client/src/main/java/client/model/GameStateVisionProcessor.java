package client.model;

import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.Terrain;

/**
 * Encapsulates vision/discovery logic derived from player position and terrain.
 */
final class GameStateVisionProcessor {

    void processVision(GameState state, MapNode currentPosition) {
        if (state == null || currentPosition == null) {
            return;
        }

        GameMap map = state.getMap();
        if (map == null) {
            return;
        }

        Terrain terrain = currentPosition.getTerrain();
        checkForDiscoveries(state, currentPosition);

        if (terrain == Terrain.MOUNTAIN) {
            processExtendedVision(state, map, currentPosition);
        }
    }

    private void processExtendedVision(GameState state, GameMap map, MapNode center) {
        int x = center.getX();
        int y = center.getY();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                try {
                    MapNode node = map.getNode(x + dx, y + dy);
                    if (node != null) {
                        checkForDiscoveries(state, node);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    private void checkForDiscoveries(GameState state, MapNode node) {
        if (!state.isOpponentFortFound() && isOpponentFortAtNode(node)) {
            state.discoverOpponentFortAt(node);
        }
    }

    private boolean isOpponentFortAtNode(MapNode node) {
        return node != null && node.isFortPresent();
    }
}
