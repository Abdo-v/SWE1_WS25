package client.model.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

final class HalfMapWaterPlacer {
    private static final int MIN_WATER_PLACEMENT_ATTEMPTS = 100;

    private HalfMapWaterPlacer() {
    }

    static boolean placeWaterWithConstraints(
            Terrain[][] grid,
            boolean[][] fort,
            int count,
            int width,
            int height,
            Random random,
            PlayerHalfMap existingHalfMap,
            MapGenerationConfig config
    ) {
        int minEdgeWaterTopBottom = (int) Math.ceil(MapRules.MIN_EDGE_BLOCKED_RATIO * width);
        int minEdgeWaterLeftRight = (int) Math.ceil(MapRules.MIN_EDGE_BLOCKED_RATIO * height);

        if (!placeMinimumWaterOnEdge(grid, fort, width, height, random, existingHalfMap, Edge.TOP, minEdgeWaterTopBottom)) {
            return false;
        }
        if (!placeMinimumWaterOnEdge(grid, fort, width, height, random, existingHalfMap, Edge.BOTTOM, minEdgeWaterTopBottom)) {
            return false;
        }
        if (!placeMinimumWaterOnEdge(grid, fort, width, height, random, existingHalfMap, Edge.LEFT, minEdgeWaterLeftRight)) {
            return false;
        }
        if (!placeMinimumWaterOnEdge(grid, fort, width, height, random, existingHalfMap, Edge.RIGHT, minEdgeWaterLeftRight)) {
            return false;
        }

        int placed = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (grid[x][y] == Terrain.WATER) {
                    placed++;
                }
            }
        }

        int attempts = 0;
        int maxAttempts = Math.max(MIN_WATER_PLACEMENT_ATTEMPTS, count * config.waterPlacementAttemptMultiplier());
        while (placed < count && attempts < maxAttempts) {
            attempts++;
            int x = random.nextInt(width);
            int y = random.nextInt(height);

            if (!canPlaceWater(grid, fort, x, y)) {
                continue;
            }

            Terrain original = grid[x][y];
            grid[x][y] = Terrain.WATER;

            boolean valid = TerrainBorderConstraints.checkBorderConstraints(grid, width, height)
                    && TerrainGridConnectivity.checkConnectivity(grid, width, height);

            if (valid) {
                placed++;
            } else {
                grid[x][y] = original;
            }
        }

        return placed == count
                && TerrainBorderConstraints.checkBorderConstraints(grid, width, height)
                && TerrainGridConnectivity.checkConnectivity(grid, width, height);
    }

    private enum Edge { TOP, BOTTOM, LEFT, RIGHT }

    private static boolean placeMinimumWaterOnEdge(
            Terrain[][] grid,
            boolean[][] fort,
            int width,
            int height,
            Random random,
            PlayerHalfMap existingHalfMap,
            Edge edge,
            int minWaterNeeded
    ) {
        int edgeLen = (edge == Edge.TOP || edge == Edge.BOTTOM) ? width : height;
        int requiredWalkable = (int) Math.ceil(MapRules.MIN_EDGE_WALKABLE_RATIO * edgeLen);
        int maxWaterAllowed = edgeLen - requiredWalkable;

        int currentWater = countWaterOnEdge(grid, width, height, edge);
        if (currentWater >= minWaterNeeded) {
            return true;
        }

        List<int[]> candidates = new ArrayList<>();
        for (int i = 0; i < edgeLen; i++) {
            int x;
            int y;
            if (edge == Edge.TOP) {
                x = i;
                y = 0;
            } else if (edge == Edge.BOTTOM) {
                x = i;
                y = height - 1;
            } else if (edge == Edge.LEFT) {
                x = 0;
                y = i;
            } else {
                x = width - 1;
                y = i;
            }
            candidates.add(new int[]{x, y});
        }

        Collections.shuffle(candidates, random);
        if (existingHalfMap != null) {
            candidates.sort((a, b) -> {
                MapNode oppositeA = getOppositeEdgeNode(existingHalfMap, edge, a[0], a[1], width, height);
                MapNode oppositeB = getOppositeEdgeNode(existingHalfMap, edge, b[0], b[1], width, height);
                boolean aBlocksCrossing = oppositeA != null && oppositeA.isWalkable();
                boolean bBlocksCrossing = oppositeB != null && oppositeB.isWalkable();
                return Boolean.compare(aBlocksCrossing, bBlocksCrossing);
            });
        }

        for (int[] pos : candidates) {
            if (currentWater >= minWaterNeeded) {
                break;
            }
            int x = pos[0];
            int y = pos[1];

            if (!canPlaceWater(grid, fort, x, y)) {
                continue;
            }

            Terrain original = grid[x][y];
            grid[x][y] = Terrain.WATER;

            int newEdgeWater = countWaterOnEdge(grid, width, height, edge);
            boolean valid = newEdgeWater <= maxWaterAllowed && TerrainGridConnectivity.checkConnectivity(grid, width, height);
            if (valid) {
                currentWater = newEdgeWater;
            } else {
                grid[x][y] = original;
            }
        }

        return currentWater >= minWaterNeeded;
    }

    private static int countWaterOnEdge(Terrain[][] grid, int width, int height, Edge edge) {
        int count = 0;
        if (edge == Edge.TOP) {
            for (int x = 0; x < width; x++) {
                if (grid[x][0] == Terrain.WATER) count++;
            }
        } else if (edge == Edge.BOTTOM) {
            for (int x = 0; x < width; x++) {
                if (grid[x][height - 1] == Terrain.WATER) count++;
            }
        } else if (edge == Edge.LEFT) {
            for (int y = 0; y < height; y++) {
                if (grid[0][y] == Terrain.WATER) count++;
            }
        } else {
            for (int y = 0; y < height; y++) {
                if (grid[width - 1][y] == Terrain.WATER) count++;
            }
        }
        return count;
    }

    private static boolean canPlaceWater(Terrain[][] grid, boolean[][] fort, int x, int y) {
        return !fort[x][y] && grid[x][y] != Terrain.WATER && grid[x][y] != Terrain.MOUNTAIN;
    }

    private static MapNode getOppositeEdgeNode(PlayerHalfMap existingHalfMap, Edge newEdge, int x, int y, int width, int height) {
        if (existingHalfMap == null) {
            return null;
        }
        if (newEdge == Edge.LEFT) {
            return existingHalfMap.getMapNode(width - 1, y).orElse(null);
        }
        if (newEdge == Edge.RIGHT) {
            return existingHalfMap.getMapNode(0, y).orElse(null);
        }
        if (newEdge == Edge.TOP) {
            return existingHalfMap.getMapNode(x, height - 1).orElse(null);
        }
        return existingHalfMap.getMapNode(x, 0).orElse(null);
    }
}
