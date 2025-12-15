package client.model.mapper;

import client.model.common.Notification;

import java.util.Random;
import java.util.Stack;

public class MapGenerator {
    private PlayerHalfMap halfMap = null;

    private static final double MIN_EDGE_WALKABLE_PERCENTAGE = 0.40;
    private static final double MIN_EDGE_BLOCKED_PERCENTAGE = 0.20;

    /**
     * Constructs a MapGenerator.
     */
    public MapGenerator() {
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
     * that edge transitions are possible on at least 40% of each edge (walkable on both sides).
     */
    public PlayerHalfMap generateMap(int width, int height, String playerID, PlayerHalfMap existingHalfMap) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }

        halfMap = null;
        boolean validMap = false;
        Random random = new Random();
        
        while (!validMap) {
            halfMap = new PlayerHalfMap(playerID);
            
            Terrain[][] terrainGrid = new Terrain[width][height];
            boolean[][] fortGrid = new boolean[width][height];
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    terrainGrid[x][y] = Terrain.GRASS;
                    fortGrid[x][y] = false;
                }
            }

            int mountainCells = random.nextInt(5,6);
            int waterCells = random.nextInt(7,8);
            int fortCells = 1;

            fortGrid = possibleFortpositions(width, height, fortCells);
            
            placeTerrain(terrainGrid, fortGrid, Terrain.MOUNTAIN, mountainCells, width, height, random);
            
            placeWaterWithConstraints(terrainGrid, fortGrid, waterCells, width, height, random, existingHalfMap);
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    MapNode node = new MapNode(x, y, terrainGrid[x][y], fortGrid[x][y], false);
                    halfMap.addMapNode(node);
                }
            }

            MapValidator validator = new MapValidator();
            Notification result = (existingHalfMap == null)
                    ? validator.validate(halfMap)
                    : validator.validate(halfMap, existingHalfMap);
            validMap = !result.hasErrors();
        }
        
        return halfMap;
    }

    /**
     * Generates a boolean grid indicating possible fort positions.
     * @param width The width of the map.
     * @param height The height of the map.
     * @return A boolean grid where true indicates a fort position.
     */
    private boolean[][] possibleFortpositions(int width, int height, int fortCells) {
        boolean[][] fortPositions = new boolean[width][height];
        Random random = new Random();
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
    private void placeWaterWithConstraints(Terrain[][] grid,
                                          boolean[][] fort,
                                          int count,
                                          int width,
                                          int height,
                                          Random random,
                                          PlayerHalfMap existingHalfMap) {
        int minEdgeWaterTopBottom = (int) Math.ceil(MIN_EDGE_BLOCKED_PERCENTAGE * width);
        int minEdgeWaterLeftRight = (int) Math.ceil(MIN_EDGE_BLOCKED_PERCENTAGE * height);

        // Place minimum required water on each edge first (to satisfy the >=20% non-walkable rule).
        placeMinimumWaterOnEdge(grid, fort, width, height, random, existingHalfMap, Edge.TOP, minEdgeWaterTopBottom);
        placeMinimumWaterOnEdge(grid, fort, width, height, random, existingHalfMap, Edge.BOTTOM, minEdgeWaterTopBottom);
        placeMinimumWaterOnEdge(grid, fort, width, height, random, existingHalfMap, Edge.LEFT, minEdgeWaterLeftRight);
        placeMinimumWaterOnEdge(grid, fort, width, height, random, existingHalfMap, Edge.RIGHT, minEdgeWaterLeftRight);

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
        int maxAttempts = Math.max(100, count * 50);

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
    }

    /**
     * Checks edge constraints: per edge at least 40% walkable (non-water) and at least 20% blocked (water).
     */
    private boolean checkBorderConstraints(Terrain[][] grid, int width, int height) {
        int requiredTopBottomWalkable = (int) Math.ceil(MIN_EDGE_WALKABLE_PERCENTAGE * width);
        int requiredTopBottomBlocked = (int) Math.ceil(MIN_EDGE_BLOCKED_PERCENTAGE * width);
        int requiredLeftRightWalkable = (int) Math.ceil(MIN_EDGE_WALKABLE_PERCENTAGE * height);
        int requiredLeftRightBlocked = (int) Math.ceil(MIN_EDGE_BLOCKED_PERCENTAGE * height);

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

    private void placeMinimumWaterOnEdge(Terrain[][] grid,
                                        boolean[][] fort,
                                        int width,
                                        int height,
                                        Random random,
                                        PlayerHalfMap existingHalfMap,
                                        Edge edge,
                                        int minWaterNeeded) {
        int edgeLen = (edge == Edge.TOP || edge == Edge.BOTTOM) ? width : height;
        int requiredWalkable = (int) Math.ceil(MIN_EDGE_WALKABLE_PERCENTAGE * edgeLen);
        int maxWaterAllowed = edgeLen - requiredWalkable;

        int currentWater = countWaterOnEdge(grid, width, height, edge);
        int attempts = 0;
        int maxAttempts = 500;

        while (currentWater < minWaterNeeded && attempts < maxAttempts) {
            attempts++;
            int x;
            int y;
            if (edge == Edge.TOP) {
                y = 0;
                x = random.nextInt(width);
            } else if (edge == Edge.BOTTOM) {
                y = height - 1;
                x = random.nextInt(width);
            } else if (edge == Edge.LEFT) {
                x = 0;
                y = random.nextInt(height);
            } else {
                x = width - 1;
                y = random.nextInt(height);
            }

            if (!canPlaceWater(grid, fort, x, y)) {
                continue;
            }

            // Prefer placing water where it does NOT reduce potential crossable transitions.
            if (existingHalfMap != null) {
                MapNode opposite = getOppositeEdgeNode(existingHalfMap, edge, x, y, width, height);
                if (opposite != null && opposite.isWalkable()) {
                    // Try another coordinate first.
                    // This is a soft preference; we'll still place later if needed.
                    if (attempts < maxAttempts / 2) {
                        continue;
                    }
                }
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
            return existingHalfMap.getMapNode(width - 1, y);
        }
        if (newEdge == Edge.RIGHT) {
            return existingHalfMap.getMapNode(0, y);
        }
        if (newEdge == Edge.TOP) {
            return existingHalfMap.getMapNode(x, height - 1);
        }
        // BOTTOM
        return existingHalfMap.getMapNode(x, 0);
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
        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{startX, startY});
        
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        
        while (!stack.isEmpty()) {
            int[] pos = stack.pop();
            int x = pos[0], y = pos[1];
            
            if (x < 0 || x >= width || y < 0 || y >= height || 
                visited[x][y] || grid[x][y] == Terrain.WATER) {
                continue;
            }
            
            visited[x][y] = true;
            
            for (int[] dir : directions) {
                stack.push(new int[]{x + dir[0], y + dir[1]});
            }
        }
    }
}