package client.model.mapper;

import client.model.common.Notification;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;

public class MapGenerator {
    private static final int[][] CARDINAL_DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private static final int MIN_WATER_PLACEMENT_ATTEMPTS = 100;

    private final MapGenerationConfig config;
    private final Supplier<Random> randomSupplier;
    private final MapValidator validator;

    /**
     * Constructs a MapGenerator.
     */
    public MapGenerator() {
        this(MapGenerationConfig.defaultConfig(), Random::new, new MapValidator());
    }

    /**
     * Constructs a MapGenerator with custom generation parameters.
     * @param config Generation configuration.
     */
    public MapGenerator(MapGenerationConfig config) {
        this(config, Random::new, new MapValidator());
    }

    /**
     * Constructs a MapGenerator with injected dependencies.
     *
     * <p>Inject a deterministic {@link Random} supplier (e.g. {@code () -> new Random(123)})
     * and a {@link MapValidator} to make generation predictable and easier to test.
     */
    public MapGenerator(MapGenerationConfig config, Supplier<Random> randomSupplier, MapValidator validator) {
        this.config = Objects.requireNonNull(config, "config");
        this.randomSupplier = Objects.requireNonNull(randomSupplier, "randomSupplier");
        this.validator = Objects.requireNonNull(validator, "validator");
    }

    /**
     * Generates a half map with the specified width, height, and player ID.
     * @param width The width of the map.
     * @param height The height of the map.
     * @param playerID The ID of the player.
     * @return The generated PlayerHalfMap.
     * @throws IllegalArgumentException if width or height is not positive.
     */
    public PlayerHalfMap generateMap(int width, int height, String playerID) {
        return generateMap(width, height, playerID, null);
    }

    /**
     * Generates a half map with the specified width, height, and player ID.
     * If an existing half map is provided (for the "second" client), generation also tries to ensure
        * that edge transitions are possible on at least {@link MapRules#MIN_EDGE_CROSSABLE_RATIO} of each edge
        * (walkable on both sides).
     */
    public PlayerHalfMap generateMap(int width, int height, String playerID, PlayerHalfMap existingHalfMap) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }

        Objects.requireNonNull(playerID, "playerID");

        boolean validMap = false;
    Random random = randomSupplier.get();
        
        while (!validMap) {
            PlayerHalfMap halfMap = new PlayerHalfMap(playerID);
            
            Terrain[][] terrainGrid = new Terrain[width][height];
            boolean[][] fortGrid = new boolean[width][height];
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    terrainGrid[x][y] = Terrain.GRASS;
                    fortGrid[x][y] = false;
                }
            }

            int mountainCells = randomInInclusiveRange(random, config.minMountainTiles(), config.maxMountainTiles());
            int waterCells = randomInInclusiveRange(random, config.minWaterTiles(), config.maxWaterTiles());
            int fortCells = config.fortTiles();

            fortGrid = possibleFortPositions(width, height, fortCells, random);
            
            placeTerrain(terrainGrid, fortGrid, Terrain.MOUNTAIN, mountainCells, width, height, random);
            
            if (!placeWaterWithConstraints(terrainGrid, fortGrid, waterCells, width, height, random, existingHalfMap)) {
                continue;
            }

            // Extra guard: ensure we never accept a map that violates the edge rules.
            // (This matches what the server validates for non-enterable border fields.)
            if (!checkBorderConstraints(terrainGrid, width, height) || !checkConnectivity(terrainGrid, width, height)) {
                continue;
            }
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    MapNode node = new MapNode(x, y, terrainGrid[x][y], fortGrid[x][y], false);
                    halfMap.addMapNode(node);
                }
            }
            Notification result = (existingHalfMap == null)
                    ? validator.validate(halfMap)
                    : validator.validate(halfMap, existingHalfMap);
            validMap = !result.hasErrors();
            if (validMap) {
                return halfMap;
            }
        }

        throw new IllegalStateException("Map generation failed unexpectedly");
    }

    /**
     * Generates a boolean grid indicating possible fort positions.
     * @param width The width of the map.
     * @param height The height of the map.
     * @return A boolean grid where true indicates a fort position.
     */
    private boolean[][] possibleFortPositions(int width, int height, int fortCells, Random random) {
        boolean[][] fortPositions = new boolean[width][height];
        for(int fortsPlaced=0; fortsPlaced<fortCells;){
            int fortX = random.nextInt(width);
            int fortY = random.nextInt(height);
            if (fortPositions[fortX][fortY]) {
                continue; // Skip if this position already has a fort
            }
            fortPositions[fortX][fortY] = true;
            fortsPlaced++;
        }
        return fortPositions;
    } 

    /**
     * Places terrain of a specific type on the grid, avoiding the fort.
     * @param grid The terrain grid.
     * @param fort The fort grid.
     * @param type The terrain type to place.
     * @param count The number of terrain fields to place.
     * @param width The width of the map.
     * @param height The height of the map.
     * @param random The random number generator.
     */
    private void placeTerrain(Terrain[][] grid, boolean[][] fort, Terrain type, int count, 
                              int width, int height, Random random) {
        int placed = 0;
        
        while (placed < count) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            
            if (!fort[x][y] && grid[x][y] != type) {
                grid[x][y] = type;
                placed++;
            }
        }
    }

    /**
     * Places water terrain on the grid while maintaining connectivity and border requirements.
     * @param grid The terrain grid.
     * @param fort The fort grid.
     * @param count The number of water fields to place.
     * @param width The width of the map.
     * @param height The height of the map.
     * @param random The random number generator.
     */
    private boolean placeWaterWithConstraints(Terrain[][] grid,
                                          boolean[][] fort,
                                          int count,
                                          int width,
                                          int height,
                                          Random random,
                                          PlayerHalfMap existingHalfMap) {
        int minEdgeWaterTopBottom = (int) Math.ceil(MapRules.MIN_EDGE_BLOCKED_RATIO * width);
        int minEdgeWaterLeftRight = (int) Math.ceil(MapRules.MIN_EDGE_BLOCKED_RATIO * height);

        // Place minimum required water on each edge first (to satisfy the >=20% non-walkable rule).
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

        // Count currently placed water
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

            boolean valid = true;
            if (!checkBorderConstraints(grid, width, height)) {
                valid = false;
            }
            if (valid && !checkConnectivity(grid, width, height)) {
                valid = false;
            }

            if (valid) {
                placed++;
            } else {
                grid[x][y] = original;
            }
        }

        return placed == count && checkBorderConstraints(grid, width, height) && checkConnectivity(grid, width, height);
    }

    /**
     * Checks edge constraints: per edge at least 40% walkable (non-water) and at least 20% blocked (water).
     */
    private boolean checkBorderConstraints(Terrain[][] grid, int width, int height) {
        int requiredTopBottomWalkable = (int) Math.ceil(MapRules.MIN_EDGE_WALKABLE_RATIO * width);
        int requiredTopBottomBlocked = (int) Math.ceil(MapRules.MIN_EDGE_BLOCKED_RATIO * width);
        int requiredLeftRightWalkable = (int) Math.ceil(MapRules.MIN_EDGE_WALKABLE_RATIO * height);
        int requiredLeftRightBlocked = (int) Math.ceil(MapRules.MIN_EDGE_BLOCKED_RATIO * height);

        int topWalkable = 0;
        int topBlocked = 0;
        for (int x = 0; x < width; x++) {
            if (grid[x][0] == Terrain.WATER) topBlocked++; else topWalkable++;
        }

        int bottomWalkable = 0;
        int bottomBlocked = 0;
        for (int x = 0; x < width; x++) {
            if (grid[x][height - 1] == Terrain.WATER) bottomBlocked++; else bottomWalkable++;
        }

        int leftWalkable = 0;
        int leftBlocked = 0;
        for (int y = 0; y < height; y++) {
            if (grid[0][y] == Terrain.WATER) leftBlocked++; else leftWalkable++;
        }

        int rightWalkable = 0;
        int rightBlocked = 0;
        for (int y = 0; y < height; y++) {
            if (grid[width - 1][y] == Terrain.WATER) rightBlocked++; else rightWalkable++;
        }

        return (topWalkable >= requiredTopBottomWalkable && topBlocked >= requiredTopBottomBlocked &&
                bottomWalkable >= requiredTopBottomWalkable && bottomBlocked >= requiredTopBottomBlocked &&
                leftWalkable >= requiredLeftRightWalkable && leftBlocked >= requiredLeftRightBlocked &&
                rightWalkable >= requiredLeftRightWalkable && rightBlocked >= requiredLeftRightBlocked);
    }

    private enum Edge {
        TOP,
        BOTTOM,
        LEFT,
        RIGHT
    }

    private boolean placeMinimumWaterOnEdge(Terrain[][] grid,
                                        boolean[][] fort,
                                        int width,
                                        int height,
                                        Random random,
                                        PlayerHalfMap existingHalfMap,
                                        Edge edge,
                                        int minWaterNeeded) {
        int edgeLen = (edge == Edge.TOP || edge == Edge.BOTTOM) ? width : height;
        int requiredWalkable = (int) Math.ceil(MapRules.MIN_EDGE_WALKABLE_RATIO * edgeLen);
        int maxWaterAllowed = edgeLen - requiredWalkable;

        int currentWater = countWaterOnEdge(grid, width, height, edge);
        if (currentWater >= minWaterNeeded) {
            return true;
        }

        // Build a candidate list of all coordinates on this edge.
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

        // Shuffle, but bias towards positions that don't reduce crossable transitions (if we have an existing half map).
        Collections.shuffle(candidates, random);
        if (existingHalfMap != null) {
            candidates.sort((a, b) -> {
                MapNode oppositeA = getOppositeEdgeNode(existingHalfMap, edge, a[0], a[1], width, height);
                MapNode oppositeB = getOppositeEdgeNode(existingHalfMap, edge, b[0], b[1], width, height);
                boolean aBlocksCrossing = oppositeA != null && oppositeA.isWalkable();
                boolean bBlocksCrossing = oppositeB != null && oppositeB.isWalkable();
                return Boolean.compare(aBlocksCrossing, bBlocksCrossing); // prefer false first
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
            boolean valid = newEdgeWater <= maxWaterAllowed;
            if (valid && !checkConnectivity(grid, width, height)) {
                valid = false;
            }

            if (valid) {
                currentWater = newEdgeWater;
            } else {
                grid[x][y] = original;
            }
        }
        return currentWater >= minWaterNeeded;
    }

    private int countWaterOnEdge(Terrain[][] grid, int width, int height, Edge edge) {
        int count = 0;
        if (edge == Edge.TOP) {
            for (int x = 0; x < width; x++) if (grid[x][0] == Terrain.WATER) count++;
        } else if (edge == Edge.BOTTOM) {
            for (int x = 0; x < width; x++) if (grid[x][height - 1] == Terrain.WATER) count++;
        } else if (edge == Edge.LEFT) {
            for (int y = 0; y < height; y++) if (grid[0][y] == Terrain.WATER) count++;
        } else {
            for (int y = 0; y < height; y++) if (grid[width - 1][y] == Terrain.WATER) count++;
        }
        return count;
    }

    private boolean canPlaceWater(Terrain[][] grid, boolean[][] fort, int x, int y) {
        return !fort[x][y] && grid[x][y] != Terrain.WATER && grid[x][y] != Terrain.MOUNTAIN;
    }

    private MapNode getOppositeEdgeNode(PlayerHalfMap existingHalfMap,
                                       Edge newEdge,
                                       int x,
                                       int y,
                                       int width,
                                       int height) {
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
        // BOTTOM
        return existingHalfMap.getMapNode(x, 0).orElse(null);
    }

    /**
     * Checks if all non-water cells are connected using flood fill.
     * @param grid The terrain grid.
     * @param width The width of the map.
     * @param height The height of the map.
     * @return true if all non-water cells are connected, false otherwise.
     */
    private boolean checkConnectivity(Terrain[][] grid, int width, int height) {
        boolean[][] visited = new boolean[width][height];
        
        int startX = -1, startY = -1;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (grid[x][y] != Terrain.WATER) {
                    startX = x;
                    startY = y;
                    break;
                }
            }
            if (startX != -1) break;
        }

        if (startX == -1) {
            return false;
        }
        
        floodFill(grid, visited, startX, startY, width, height);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (grid[x][y] != Terrain.WATER && !visited[x][y]) {
                    return false;
                }
            }
        }
        
        return true;
    }

    /**
     * Performs a flood fill to check connectivity of non-water cells.
     * @param grid The terrain grid.
     * @param visited The visited grid.
     * @param startX The starting X coordinate.
     * @param startY The starting Y coordinate.
     * @param width The width of the map.
     * @param height The height of the map.
     */
    private void floodFill(Terrain[][] grid, boolean[][] visited, int startX, int startY, 
                          int width, int height) {
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{startX, startY});
        
        while (!stack.isEmpty()) {
            int[] pos = stack.pop();
            int x = pos[0], y = pos[1];
            
            if (x < 0 || x >= width || y < 0 || y >= height || 
                visited[x][y] || grid[x][y] == Terrain.WATER) {
                continue;
            }
            
            visited[x][y] = true;
            
            for (int[] dir : CARDINAL_DIRECTIONS) {
                stack.push(new int[]{x + dir[0], y + dir[1]});
            }
        }
    }

    private int randomInInclusiveRange(Random random, int minInclusive, int maxInclusive) {
        if (minInclusive == maxInclusive) {
            return minInclusive;
        }
        int boundExclusive = Math.addExact(maxInclusive, 1);
        return random.nextInt(minInclusive, boundExclusive);
    }
}