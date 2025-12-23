package client.model.mapper.validator;

import client.model.common.Notification;
import client.model.mapper.MapNode;
import client.model.mapper.MapRules;
import client.model.mapper.PlayerHalfMap;

import java.util.Optional;

final class HalfMapEdgeWalkabilityValidator {
    private HalfMapEdgeWalkabilityValidator() {
    }

    static void validateEdgeConstraints(PlayerHalfMap halfMap, Notification notification, int maxX, int maxY) {
        int minX = 0;
        int minY = 0;

        checkSingleEdge(halfMap, notification, minY, false, minX, maxX, "Top Edge (Y=" + minY + ")");
        checkSingleEdge(halfMap, notification, maxY, false, minX, maxX, "Bottom Edge (Y=" + maxY + ")");
        checkSingleEdge(halfMap, notification, minX, true, minY, maxY, "Left Edge (X=" + minX + ")");
        checkSingleEdge(halfMap, notification, maxX, true, minY, maxY, "Right Edge (X=" + maxX + ")");
    }

    private static void checkSingleEdge(
            PlayerHalfMap halfMap,
            Notification notification,
            int fixedCoordVal,
            boolean isXFixed,
            int startVarCoord,
            int endVarCoord,
            String edgeName
    ) {
        int totalEdgeNodes = 0;
        int walkableEdgeNodes = 0;
        int blockedEdgeNodes = 0;

        for (int varCoord = startVarCoord; varCoord <= endVarCoord; varCoord++) {
            int x;
            int y;
            if (isXFixed) {
                x = fixedCoordVal;
                y = varCoord;
            } else {
                x = varCoord;
                y = fixedCoordVal;
            }

            Optional<MapNode> nodeOpt = halfMap.getMapNode(x, y);
            if (nodeOpt.isPresent()) {
                MapNode node = nodeOpt.get();
                totalEdgeNodes++;
                if (node.isWalkable()) {
                    walkableEdgeNodes++;
                } else {
                    blockedEdgeNodes++;
                }
            } else {
                notification.addError("Critical: Missing node on " + edgeName + " at X=" + x + ", Y=" + y
                        + " during edge walkability check. Aborting this check.");
                return;
            }
        }

        if (totalEdgeNodes == 0 && (endVarCoord >= startVarCoord)) {
            notification.addError(edgeName + " has an effective length of 0 or no nodes were found, which is unexpected for a valid map structure.");
            return;
        }
        if (totalEdgeNodes == 0) {
            return;
        }

        int requiredWalkableNodes = (int) Math.ceil(MapRules.MIN_EDGE_WALKABLE_RATIO * totalEdgeNodes);
        int requiredBlockedNodes = (int) Math.ceil(MapRules.MIN_EDGE_BLOCKED_RATIO * totalEdgeNodes);

        if (walkableEdgeNodes < requiredWalkableNodes) {
            notification.addError(String.format(
                    "%s walkability below threshold. Required: >=%.0f%% (%d nodes), Found: %d/%d nodes.",
                    edgeName, MapRules.MIN_EDGE_WALKABLE_RATIO * 100, requiredWalkableNodes, walkableEdgeNodes, totalEdgeNodes));
        }
        if (blockedEdgeNodes < requiredBlockedNodes) {
            notification.addError(String.format(
                    "%s non-walkable fields below threshold. Required: >=%.0f%% (%d nodes), Found: %d/%d nodes.",
                    edgeName, MapRules.MIN_EDGE_BLOCKED_RATIO * 100, requiredBlockedNodes, blockedEdgeNodes, totalEdgeNodes));
        }
    }
}
