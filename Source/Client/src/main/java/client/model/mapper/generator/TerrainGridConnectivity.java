package client.model.mapper.generator;

import client.model.mapper.Terrain;

import java.util.ArrayDeque;
import java.util.Deque;

final class TerrainGridConnectivity {
    private static final int[][] CARDINAL_DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private TerrainGridConnectivity() {
    }

    static boolean checkConnectivity(Terrain[][] grid, int width, int height) {
        boolean[][] visited = new boolean[width][height];

        int startX = -1;
        int startY = -1;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (grid[x][y] != Terrain.WATER) {
                    startX = x;
                    startY = y;
                    break;
                }
            }
            if (startX != -1) {
                break;
            }
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

    private static void floodFill(Terrain[][] grid, boolean[][] visited, int startX, int startY, int width, int height) {
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{startX, startY});

        while (!stack.isEmpty()) {
            int[] pos = stack.pop();
            int x = pos[0];
            int y = pos[1];

            if (x < 0 || x >= width || y < 0 || y >= height || visited[x][y] || grid[x][y] == Terrain.WATER) {
                continue;
            }

            visited[x][y] = true;

            for (int[] dir : CARDINAL_DIRECTIONS) {
                stack.push(new int[]{x + dir[0], y + dir[1]});
            }
        }
    }
}
