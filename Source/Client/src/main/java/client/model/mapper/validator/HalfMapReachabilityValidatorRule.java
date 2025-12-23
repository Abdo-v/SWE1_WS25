package client.model.mapper.validator;

import client.model.common.Notification;
import client.model.mapper.PlayerHalfMap;

final class HalfMapReachabilityValidatorRule implements HalfMapValidationRule {

    private final double minGrassPercentage;
    private final double minMountainPercentage;

    HalfMapReachabilityValidatorRule(double minGrassPercentage, double minMountainPercentage) {
        this.minGrassPercentage = minGrassPercentage;
        this.minMountainPercentage = minMountainPercentage;
    }

    @Override
    public HalfMapRulePhase phase() {
        return HalfMapRulePhase.ADVANCED;
    }

    @Override
    public void validate(PlayerHalfMap halfMap, HalfMapValidationContext context, Notification notification) {
        requireArgs(halfMap, context, notification);
        HalfMapReachabilityValidator.validateReachability(
                halfMap,
                notification,
                context.maxX(),
                context.maxY(),
                minGrassPercentage,
                minMountainPercentage);
    }
}
