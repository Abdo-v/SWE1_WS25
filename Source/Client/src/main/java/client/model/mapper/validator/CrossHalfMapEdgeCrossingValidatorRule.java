package client.model.mapper.validator;

import client.model.common.Notification;
import client.model.mapper.PlayerHalfMap;

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
