package client.model.mapper.generator;

import client.model.ModelTextConfig;
import client.model.mapper.MapRules;

/**
 * Configuration values for {@link MapGenerator}.
 *
 * <p>This record centralizes all generation parameters to avoid magic numbers and
 * makes map generation behavior easier to test and tweak.
 */
public record MapGenerationConfig(
        int minMountainTiles,
        int maxMountainTiles,
        int minWaterTiles,
        int maxWaterTiles,
        int fortTiles,
        double minBorderWalkableRatio,
        int waterPlacementAttemptMultiplier
) {

    public MapGenerationConfig {
        if (minMountainTiles < 0 || maxMountainTiles < 0 || minWaterTiles < 0 || maxWaterTiles < 0) {
            throw new IllegalArgumentException(ModelTextConfig.ERROR_TILE_COUNTS_NON_NEGATIVE);
        }
        if (minMountainTiles > maxMountainTiles) {
            throw new IllegalArgumentException(ModelTextConfig.ERROR_MIN_MOUNTAIN_LE_MAX);
        }
        if (minWaterTiles > maxWaterTiles) {
            throw new IllegalArgumentException(ModelTextConfig.ERROR_MIN_WATER_LE_MAX);
        }
        if (fortTiles <= 0) {
            throw new IllegalArgumentException(ModelTextConfig.ERROR_FORT_TILES_POSITIVE);
        }
        if (!(minBorderWalkableRatio > 0.0 && minBorderWalkableRatio <= 1.0)) {
            throw new IllegalArgumentException(ModelTextConfig.ERROR_MIN_BORDER_RATIO_RANGE);
        }
        if (waterPlacementAttemptMultiplier <= 0) {
            throw new IllegalArgumentException(ModelTextConfig.ERROR_WATER_ATTEMPT_MULTIPLIER_POSITIVE);
        }
    }

    public static MapGenerationConfig defaultConfig() {
        return new MapGenerationConfig(
                5,
                7,
                7,
                8,
                1,
                MapRules.MIN_EDGE_WALKABLE_RATIO,
                50
        );
    }
}
