package client.model.mapper.generator;

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
            throw new IllegalArgumentException("Tile counts must be non-negative");
        }
        if (minMountainTiles > maxMountainTiles) {
            throw new IllegalArgumentException("minMountainTiles must be <= maxMountainTiles");
        }
        if (minWaterTiles > maxWaterTiles) {
            throw new IllegalArgumentException("minWaterTiles must be <= maxWaterTiles");
        }
        if (fortTiles <= 0) {
            throw new IllegalArgumentException("fortTiles must be positive");
        }
        if (!(minBorderWalkableRatio > 0.0 && minBorderWalkableRatio <= 1.0)) {
            throw new IllegalArgumentException("minBorderWalkableRatio must be in (0, 1]");
        }
        if (waterPlacementAttemptMultiplier <= 0) {
            throw new IllegalArgumentException("waterPlacementAttemptMultiplier must be positive");
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
