package client.model.mapper.generator;

import java.util.Random;

final class MapGenerationRandom {
    private MapGenerationRandom() {
    }

    static int inInclusiveRange(Random random, int minInclusive, int maxInclusive) {
        if (minInclusive == maxInclusive) {
            return minInclusive;
        }
        int boundExclusive = Math.addExact(maxInclusive, 1);
        return random.nextInt(minInclusive, boundExclusive);
    }
}
