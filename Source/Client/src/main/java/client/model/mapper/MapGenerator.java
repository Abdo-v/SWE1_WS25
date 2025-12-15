package client.model.mapper;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Random;

public class MapGenerator {
    private static final int[][] CARDINAL_DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private final MapGenerationConfig config;

    /**
     * Constructs a MapGenerator.
     */
    public MapGenerator() {
        this(MapGenerationConfig.defaultConfig());
    }

    /**
     * Constructs a MapGenerator with custom generation parameters.
     * @param config Generation configuration.
     */
    public MapGenerator(MapGenerationConfig config) {
        this.config = Objects.requireNonNull(config, "config");
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
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }

        Objects.requireNonNull(playerID, "playerID");

        boolean validMap = false;
        Random random = new Random();
        
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
            
            placeWaterWithConstraints(terrainGrid, fortGrid, waterCells, width, height, random);
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    MapNode node = new MapNode(x, y, terrainGrid[x][y], fortGrid[x][y], false);
                    halfMap.addMapNode(node);
                }
            }
            
            if (checkBorderWalkability(terrainGrid, width, height) && 
                checkConnectivity(terrainGrid, width, height)) {
                validMap = true;
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
    private void placeWaterWithConstraints(Terrain[][] grid, boolean[][] fort, int count, 
                                          int width, int height, Random random) {
        int placed = 0;
        int attempts = 0;
        int maxAttempts = count * config.waterPlacementAttemptMultiplier();
        
        while (placed < count && attempts < maxAttempts) {
            attempts++;
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            
            if (fort[x][y] || grid[x][y] == Terrain.WATER || grid[x][y] == Terrain.MOUNTAIN) {
                continue;
            }
            
            boolean isBorder = (x == 0 || x == width-1 || y == 0 || y == height-1);
            
            Terrain original = grid[x][y];
            grid[x][y] = Terrain.WATER;
            
            boolean valid = true;
            if (isBorder && !checkBorderWalkability(grid, width, height)) {
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
     * Checks if at least 51% of each border is walkable (non-water).
     * @param grid The terrain grid.
     * @param width The width of the map.
     * @param height The height of the map.
     * @return true if at least 51% of each border is walkable, false otherwise.
     */
    private boolean checkBorderWalkability(Terrain[][] grid, int width, int height) {
        int walkableBottom = 0;
        for (int x = 0; x < width; x++) {
            if (grid[x][0] != Terrain.WATER) {
                walkableBottom++;
            }
        }
        
        int walkableTop = 0;
        for (int x = 0; x < width; x++) {
            if (grid[x][height-1] != Terrain.WATER) {
                walkableTop++;
            }
        }
        
        int walkableLeft = 0;
        for (int y = 0; y < height; y++) {
            if (grid[0][y] != Terrain.WATER) {
                walkableLeft++;
            }
        }
        
        int walkableRight = 0;
        for (int y = 0; y < height; y++) {
            if (grid[width-1][y] != Terrain.WATER) {
                walkableRight++;
            }
        }

        int requiredWidth = (int) Math.ceil(width * config.minBorderWalkableRatio());
        int requiredHeight = (int) Math.ceil(height * config.minBorderWalkableRatio());
        
        return (walkableBottom >= requiredWidth &&
                walkableTop >= requiredWidth &&
                walkableLeft >= requiredHeight &&
                walkableRight >= requiredHeight);
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