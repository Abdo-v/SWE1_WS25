package client.model.mapper.validator;

import client.model.common.Notification;
import client.model.mapper.PlayerHalfMap;

final class HalfMapTerrainAndFortValidatorRule implements HalfMapValidationRule {

    private final int expectedTotalNodes;
    private final double minMountainPercentage;
    private final double minGrassPercentage;
    private final double minWaterPercentage;
    private final double fortPercentage;

    HalfMapTerrainAndFortValidatorRule(
            int expectedTotalNodes,
            double minMountainPercentage,
            double minGrassPercentage,
            double minWaterPercentage,
            double fortPercentage
    ) {
        this.expectedTotalNodes = expectedTotalNodes;
        this.minMountainPercentage = minMountainPercentage;
        this.minGrassPercentage = minGrassPercentage;
        this.minWaterPercentage = minWaterPercentage;
        this.fortPercentage = fortPercentage;
    }

    @Override
    public HalfMapRulePhase phase() {
        return HalfMapRulePhase.BASIC;
    }

    @Override
    public void validate(PlayerHalfMap halfMap, HalfMapValidationContext context, Notification notification) {
        requireArgs(halfMap, context, notification);
        HalfMapTerrainValidator.validateTerrainAndFort(
                halfMap,
                notification,
                expectedTotalNodes,
                minMountainPercentage,
                minGrassPercentage,
                minWaterPercentage,
                fortPercentage);
    }
}
