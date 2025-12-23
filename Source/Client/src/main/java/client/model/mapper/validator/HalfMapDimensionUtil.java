package client.model.mapper.validator;

import client.model.mapper.MapNode;
import client.model.mapper.PlayerHalfMap;

import java.util.List;
import java.util.Objects;

final class HalfMapDimensionUtil {
    private HalfMapDimensionUtil() {
    }

    static int[] determineDimensions(PlayerHalfMap halfMap) {
        List<MapNode> nodes = Objects.requireNonNull(halfMap, "halfMap").getMapNodes();
        if (nodes.isEmpty()) {
            return new int[]{0, 0};
        }
        int maxX = -1;
        int maxY = -1;
        for (MapNode node : nodes) {
            maxX = Math.max(maxX, node.getX());
            maxY = Math.max(maxY, node.getY());
        }
        return new int[]{maxX + 1, maxY + 1};
    }
}
