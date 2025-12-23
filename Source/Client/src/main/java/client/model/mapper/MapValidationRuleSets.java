package client.model.mapper;

import java.util.List;

/**
 * Central registration point for map validation rules.
 *
 * <p>To extend validation, register additional rules here (or inject custom rule lists via
 * {@link MapValidator#MapValidator(List, List)}).
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
