package client.view;

import client.model.mapper.GameMap;
import client.model.mapper.MapNode;

import java.util.Optional;

final class SafeMapNodeLookup {

    private SafeMapNodeLookup() {
    }

    static Optional<MapNode> tryGetNode(GameMap map, int x, int y) {
        try {
            return Optional.ofNullable(map.getNode(x, y));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
