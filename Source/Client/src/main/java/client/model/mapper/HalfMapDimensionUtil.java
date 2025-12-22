package client.model.mapper;

import java.util.List;

final class HalfMapDimensionUtil {
    private HalfMapDimensionUtil() {
    }

    static int[] determineDimensions(PlayerHalfMap halfMap) {
        List<MapNode> nodes = halfMap.getMapNodes();
        if (nodes == null || nodes.isEmpty()) {
            return new int[]{0, 0};
        }
        int maxX = -1;
        int maxY = -1;
        for (MapNode node : nodes) {
            if (node == null) {
                continue;
            }
            maxX = Math.max(maxX, node.getX());
            maxY = Math.max(maxY, node.getY());
        }
        return new int[]{maxX + 1, maxY + 1};
    }
}
