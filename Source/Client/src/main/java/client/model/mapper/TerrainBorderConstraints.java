package client.model.mapper;

final class TerrainBorderConstraints {
    private TerrainBorderConstraints() {
    }

    /**
     * Checks edge constraints: per edge at least 40% walkable (non-water) and at least 20% blocked (water).
     */
    static boolean checkBorderConstraints(Terrain[][] grid, int width, int height) {
        int requiredTopBottomWalkable = (int) Math.ceil(MapRules.MIN_EDGE_WALKABLE_RATIO * width);
        int requiredTopBottomBlocked = (int) Math.ceil(MapRules.MIN_EDGE_BLOCKED_RATIO * width);
        int requiredLeftRightWalkable = (int) Math.ceil(MapRules.MIN_EDGE_WALKABLE_RATIO * height);
        int requiredLeftRightBlocked = (int) Math.ceil(MapRules.MIN_EDGE_BLOCKED_RATIO * height);

        int topWalkable = 0;
        int topBlocked = 0;
        for (int x = 0; x < width; x++) {
            if (grid[x][0] == Terrain.WATER) {
                topBlocked++;
            } else {
                topWalkable++;
            }
        }

        int bottomWalkable = 0;
        int bottomBlocked = 0;
        for (int x = 0; x < width; x++) {
            if (grid[x][height - 1] == Terrain.WATER) {
                bottomBlocked++;
            } else {
                bottomWalkable++;
            }
        }

        int leftWalkable = 0;
        int leftBlocked = 0;
        for (int y = 0; y < height; y++) {
            if (grid[0][y] == Terrain.WATER) {
                leftBlocked++;
            } else {
                leftWalkable++;
            }
        }

        int rightWalkable = 0;
        int rightBlocked = 0;
        for (int y = 0; y < height; y++) {
            if (grid[width - 1][y] == Terrain.WATER) {
                rightBlocked++;
            } else {
                rightWalkable++;
            }
        }

        return topWalkable >= requiredTopBottomWalkable && topBlocked >= requiredTopBottomBlocked
                && bottomWalkable >= requiredTopBottomWalkable && bottomBlocked >= requiredTopBottomBlocked
                && leftWalkable >= requiredLeftRightWalkable && leftBlocked >= requiredLeftRightBlocked
                && rightWalkable >= requiredLeftRightWalkable && rightBlocked >= requiredLeftRightBlocked;
    }
}
