package client.model.mapper.validator;

import client.model.common.Notification;
import client.model.mapper.PlayerHalfMap;

final class HalfMapEdgeConstraintsValidatorRule implements HalfMapValidationRule {

    @Override
    public HalfMapRulePhase phase() {
        return HalfMapRulePhase.ADVANCED;
    }

    @Override
    public void validate(PlayerHalfMap halfMap, HalfMapValidationContext context, Notification notification) {
        requireArgs(halfMap, context, notification);
        HalfMapEdgeValidator.validateEdgeConstraints(halfMap, notification, context.maxX(), context.maxY());
    }
}
