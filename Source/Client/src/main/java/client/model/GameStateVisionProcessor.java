package client.model;

import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.Terrain;

import java.util.Objects;

/**
 * Encapsulates vision/discovery logic derived from player position and terrain.
 */
final class GameStateVisionProcessor {

    void processVision(GameState state, MapNode currentPosition) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(currentPosition, "currentPosition");

        state.getMap().ifPresent(map -> {
            Terrain terrain = currentPosition.getTerrain();
            checkForDiscoveries(state, currentPosition);

            if (terrain == Terrain.MOUNTAIN) {
                processExtendedVision(state, map, currentPosition);
            }
        });
    }

    private void processExtendedVision(GameState state, GameMap map, MapNode center) {
        int x = center.getX();
        int y = center.getY();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                try {
                    MapNode node = map.getNode(x + dx, y + dy);
                    checkForDiscoveries(state, node);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    private void checkForDiscoveries(GameState state, MapNode node) {
        if (!state.isOpponentFortFound() && node.isFortPresent()) {
            state.discoverOpponentFortAt(node);
        }
    }
}
