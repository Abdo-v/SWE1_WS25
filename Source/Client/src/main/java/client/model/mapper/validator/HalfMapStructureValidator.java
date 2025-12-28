package client.model.mapper.validator;

import client.model.common.Notification;
import client.model.mapper.HalfMapDimensions;
import client.model.mapper.MapNode;
import client.model.mapper.PlayerHalfMap;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Validates basic structural invariants of a half-map (size, grid completeness, unique coordinates).
 *
 * <p>On success, returns the computed bounds used by later validation phases.
 */
final class HalfMapStructureValidator {
    private HalfMapStructureValidator() {
    }

    static Optional<HalfMapBounds> validateAndGetBounds(PlayerHalfMap halfMap, Notification notification, int expectedTotalNodes) {
        List<MapNode> nodes = Objects.requireNonNull(halfMap, "halfMap").getMapNodes();

        if (nodes.size() != expectedTotalNodes) {
            notification.addError("Map must contain exactly " + expectedTotalNodes + " nodes. Found: " + nodes.size());
            return Optional.empty();
        }

        int maxX = -1;
        int maxY = -1;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        Set<String> uniqueCoords = new HashSet<>();
        for (MapNode node : nodes) {
            Objects.requireNonNull(node, "MapNode list contains a missing node entry");
            maxX = Math.max(maxX, node.getX());
            maxY = Math.max(maxY, node.getY());
            minX = Math.min(minX, node.getX());
            minY = Math.min(minY, node.getY());
            if (!uniqueCoords.add(node.getX() + "," + node.getY())) {
                notification.addError("Duplicate coordinates found at X=" + node.getX() + ", Y=" + node.getY());
            }
        }

        if (notification.hasErrors()) {
            return Optional.empty();
        }

        if (minX != 0 || minY != 0) {
            notification.addError("Map coordinates should start from (0,0). Found minX=" + minX + ", minY=" + minY);
        }

        int inferredWidth = maxX + 1;
        int inferredHeight = maxY + 1;
        boolean dimensionsValid = (inferredWidth == HalfMapDimensions.WIDTH && inferredHeight == HalfMapDimensions.HEIGHT)
                || (inferredWidth == HalfMapDimensions.HEIGHT && inferredHeight == HalfMapDimensions.WIDTH);

        if (!dimensionsValid) {
            notification.addError(String.format(
                    "Invalid map dimensions. Expected %dx%d or %dx%d, but got %dx%d (maxX=%d, maxY=%d).",
                    HalfMapDimensions.WIDTH,
                    HalfMapDimensions.HEIGHT,
                    HalfMapDimensions.HEIGHT,
                    HalfMapDimensions.WIDTH,
                    inferredWidth,
                    inferredHeight,
                    maxX,
                    maxY));
        } else {
            for (int x = 0; x <= maxX; x++) {
                for (int y = 0; y <= maxY; y++) {
                    if (halfMap.getMapNode(x, y).isEmpty()) {
                        notification.addError("Missing map node at coordinates X=" + x + ", Y=" + y
                                + " within the " + (maxX + 1) + "x" + (maxY + 1) + " grid.");
                    }
                }
            }
        }

        if (notification.hasErrors()) {
            return Optional.empty();
        }

        return Optional.of(new HalfMapBounds(maxX, maxY));
    }
}
