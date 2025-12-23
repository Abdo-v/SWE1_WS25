package client.model.mapper.generator;

import client.model.mapper.Terrain;

import java.util.Random;

final class TerrainGridPlacer {
    private TerrainGridPlacer() {
    }

    static void placeTerrain(Terrain[][] grid, boolean[][] fort, Terrain type, int count, int width, int height, Random random) {
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
}
