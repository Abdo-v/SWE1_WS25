package client.model.mapper.generator;

import client.model.mapper.Terrain;

import java.util.Random;

final class HalfMapTerrainSetup {
    private HalfMapTerrainSetup() {
    }

    static void initializeWithGrassAndNoFort(Terrain[][] terrainGrid, boolean[][] fortGrid, int width, int height) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                terrainGrid[x][y] = Terrain.GRASS;
                fortGrid[x][y] = false;
            }
        }
    }

    static boolean[][] possibleFortPositions(int width, int height, int fortCells, Random random) {
        boolean[][] fortPositions = new boolean[width][height];
        for (int fortsPlaced = 0; fortsPlaced < fortCells; ) {
            int fortX = random.nextInt(width);
            int fortY = random.nextInt(height);
            if (fortPositions[fortX][fortY]) {
                continue;
            }
            fortPositions[fortX][fortY] = true;
            fortsPlaced++;
        }
        return fortPositions;
    }
}
