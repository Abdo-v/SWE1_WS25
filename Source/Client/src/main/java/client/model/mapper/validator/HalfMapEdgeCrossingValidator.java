package client.model.mapper.validator;

import client.model.common.Notification;
import client.model.mapper.MapNode;
import client.model.mapper.MapRules;
import client.model.mapper.PlayerHalfMap;

import java.util.Optional;

/** Checks whether two half-maps allow sufficient walkable transitions across their touching edge. */
final class HalfMapEdgeCrossingValidator {
    private HalfMapEdgeCrossingValidator() {
    }

    static void validateEdgeCrossingCompatibility(
            PlayerHalfMap newHalfMap,
            PlayerHalfMap existingHalfMap,
            Notification notification,
            int maxX,
            int maxY
    ) {
        checkCrossingOnVerticalEdge(newHalfMap, existingHalfMap, notification,
                0, maxX, maxY, "Left Edge (new X=0) vs Right Edge (existing X=" + maxX + ")");

        checkCrossingOnVerticalEdge(newHalfMap, existingHalfMap, notification,
                maxX, 0, maxY, "Right Edge (new X=" + maxX + ") vs Left Edge (existing X=0)");

        checkCrossingOnHorizontalEdge(newHalfMap, existingHalfMap, notification,
                0, maxY, maxX, "Top Edge (new Y=0) vs Bottom Edge (existing Y=" + maxY + ")");

        checkCrossingOnHorizontalEdge(newHalfMap, existingHalfMap, notification,
                maxY, 0, maxX, "Bottom Edge (new Y=" + maxY + ") vs Top Edge (existing Y=0)");
    }

    private static void checkCrossingOnVerticalEdge(
            PlayerHalfMap newHalfMap,
            PlayerHalfMap existingHalfMap,
            Notification notification,
            int newX,
            int existingX,
            int maxY,
            String label
    ) {
        int total = maxY + 1;
        int crossable = 0;
        for (int y = 0; y <= maxY; y++) {
            Optional<MapNode> n1Opt = newHalfMap.getMapNode(newX, y);
            Optional<MapNode> n2Opt = existingHalfMap.getMapNode(existingX, y);
            if (n1Opt.isEmpty() || n2Opt.isEmpty()) {
                notification.addError("Missing node(s) while checking crossing: " + label + " at y=" + y);
                return;
            }
            if (n1Opt.get().isWalkable() && n2Opt.get().isWalkable()) {
                crossable++;
            }
        }

        int required = (int) Math.ceil(MapRules.MIN_EDGE_CROSSABLE_RATIO * total);
        if (crossable < required) {
            notification.addError(String.format(
                    "Crossing compatibility below threshold for %s. Required: >=%.0f%% (%d fields), Found: %d/%d fields.",
                    label, MapRules.MIN_EDGE_CROSSABLE_RATIO * 100, required, crossable, total));
        }
    }

    private static void checkCrossingOnHorizontalEdge(
            PlayerHalfMap newHalfMap,
            PlayerHalfMap existingHalfMap,
            Notification notification,
            int newY,
            int existingY,
            int maxX,
            String label
    ) {
        int total = maxX + 1;
        int crossable = 0;
        for (int x = 0; x <= maxX; x++) {
            Optional<MapNode> n1Opt = newHalfMap.getMapNode(x, newY);
            Optional<MapNode> n2Opt = existingHalfMap.getMapNode(x, existingY);
            if (n1Opt.isEmpty() || n2Opt.isEmpty()) {
                notification.addError("Missing node(s) while checking crossing: " + label + " at x=" + x);
                return;
            }
            if (n1Opt.get().isWalkable() && n2Opt.get().isWalkable()) {
                crossable++;
            }
        }

        int required = (int) Math.ceil(MapRules.MIN_EDGE_CROSSABLE_RATIO * total);
        if (crossable < required) {
            notification.addError(String.format(
                    "Crossing compatibility below threshold for %s. Required: >=%.0f%% (%d fields), Found: %d/%d fields.",
                    label, MapRules.MIN_EDGE_CROSSABLE_RATIO * 100, required, crossable, total));
        }
    }
}
