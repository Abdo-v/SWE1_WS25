package client.model.mapper.validator;

import java.util.List;

/**
 * Central registration point for map validation rules.
 */
final class MapValidationRuleSets {

    private MapValidationRuleSets() {
    }

    static List<HalfMapValidationRule> defaultHalfMapRules(
            int expectedTotalNodes,
            double minMountainPercentage,
            double minGrassPercentage,
            double minWaterPercentage,
            double fortPercentage
    ) {
        return List.of(
                new HalfMapTerrainAndFortValidatorRule(
                        expectedTotalNodes,
                        minMountainPercentage,
                        minGrassPercentage,
                        minWaterPercentage,
                        fortPercentage),
                new HalfMapReachabilityValidatorRule(minGrassPercentage, minMountainPercentage),
                new HalfMapEdgeConstraintsValidatorRule()
        );
    }

    static List<CrossHalfMapValidationRule> defaultCrossHalfMapRules() {
        return List.of(
                new CrossHalfMapEdgeCrossingValidatorRule()
        );
    }
}
