package client.model.mapper;

import client.model.common.Notification;

final class HalfMapEdgeValidator {
    private HalfMapEdgeValidator() {
    }

    static void validateEdgeConstraints(PlayerHalfMap halfMap, Notification notification, int maxX, int maxY) {
        HalfMapEdgeWalkabilityValidator.validateEdgeConstraints(halfMap, notification, maxX, maxY);
    }

    static void validateEdgeCrossingCompatibility(
            PlayerHalfMap newHalfMap,
            PlayerHalfMap existingHalfMap,
            Notification notification,
            int maxX,
            int maxY
    ) {
        HalfMapEdgeCrossingValidator.validateEdgeCrossingCompatibility(
                newHalfMap,
                existingHalfMap,
                notification,
                maxX,
                maxY);
    }
}
