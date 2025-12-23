package client.model.mapper;

import client.model.common.Notification;

final class CrossHalfMapEdgeCrossingValidatorRule implements CrossHalfMapValidationRule {

    @Override
    public void validate(
            PlayerHalfMap newHalfMap,
            PlayerHalfMap existingHalfMap,
            CrossHalfMapValidationContext context,
            Notification notification
    ) {
        requireArgs(newHalfMap, existingHalfMap, context, notification);
        HalfMapEdgeValidator.validateEdgeCrossingCompatibility(
                newHalfMap,
                existingHalfMap,
                notification,
                context.maxX(),
                context.maxY());
    }
}
