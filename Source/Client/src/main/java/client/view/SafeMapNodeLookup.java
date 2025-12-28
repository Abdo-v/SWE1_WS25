package client.view;

import client.model.mapper.GameMap;
import client.model.mapper.MapNode;

import java.util.Optional;

/**
 * Safe accessor for map nodes that tolerates incomplete/partial map data.
 *
 * <p>Some views iterate coordinate ranges based on max bounds; during early game phases the
 * underlying map representation may be incomplete or throw for missing cells. This helper
 * converts such cases into {@link Optional#empty()}.
 */
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
