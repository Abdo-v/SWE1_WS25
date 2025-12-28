package client.model.mapper.validator;

import client.model.common.Notification;
import client.model.mapper.PlayerHalfMap;

/** Entry point for edge-related validations (walkability ratios and cross-half compatibility). */
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
