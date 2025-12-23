package client.model.mapper;

import client.model.common.Notification;

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
