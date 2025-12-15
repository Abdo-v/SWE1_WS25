package client.view;

import client.model.GameState;
import client.model.GameMode;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.PlayerHalfMap;

public class CLIHandler implements client.observer.util.Observer {

    private GameState gameState;
    private static boolean reduced;

    public CLIHandler() {
        this.gameState = null;
    }

    public CLIHandler(String gameMode){
        this(GameMode.fromCliValue(gameMode));
    }

    public CLIHandler(GameMode gameMode) {
        CLIHandler.reduced = gameMode != null && gameMode.isReduced();
    }

    public CLIHandler(GameState gameState) {
        this.gameState = gameState;
    }

    @Override
    public void update(GameState gameState ){
        this.gameState = gameState;
        // to do : implement update on the output to the CLI
    }

    public static boolean isGameModeReduced() {
        return CLIHandler.reduced;
    }

    /**
     * Visualizes the half map or full map in console output.
     * @param mapType The type of map to visualize: "own", "opponent", or "full".
     */
    public void visualizeMap(String mapType) {
        visualizeMap(MapVisualizationType.fromCliValue(mapType));
    }

    public void visualizeMap(MapVisualizationType mapType) {
        String label = mapType != null ? mapType.cliValue() : "unknown";
        System.out.println("Map Visualization: " + label.toUpperCase());
        System.out.println("=================================");
        switch (mapType) {
            case OWN:
                if (gameState.getMap() != null && gameState.getMap().getContentSize() != 0) {
                    printHalfMap(gameState.getMap().getOwnHalfMap(), "Own Half Map");
                } else {
                    System.out.println("Own half map visual: gameMap null");
                }
                break;
            case OPPONENT:
                if (gameState.getMap() != null && gameState.getMap().getContentSize() == 100) {
                    printHalfMap(gameState.getMap().getOpponentHalfMap(), "Opponent Half Map");
                } else {
                    System.out.println("Opponent half map visual gameMap null or not 100.");
                }
                break;
            case FULL:
                if (gameState.getMap() != null && gameState.getMap().getContentSize() == 100) {
                    printFullMap(gameState.getMap());
                } else {
                    System.out.println("Full map visual: gameState.getMap() null or not 100.");
                }
                break;
            default:
                System.out.println("Invalid map type. Use 'own', 'opponent', or 'full'.");
        }
    }

    /**
     * Prints a half map representation to the console.
     * @param halfMap The half map to print.
     * @param title Title for the map visualization.
     */
    public void printHalfMap(PlayerHalfMap halfMap, String title) {
        if (halfMap == null || halfMap.getMapNodes().isEmpty()) {
            System.out.println("No map data available.");
            return;
        }
        System.out.println("\n" + title + " (10x5):");
        System.out.println("-------------------------");
        java.util.Map<String, MapNode> nodeMap = new java.util.HashMap<>();
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (MapNode node : halfMap.getMapNodes()) {
            String key = node.getX() + "," + node.getY();
            nodeMap.put(key, node);
            minX = Math.min(minX, node.getX());
            maxX = Math.max(maxX, node.getX());
            minY = Math.min(minY, node.getY());
            maxY = Math.max(maxY, node.getY());
        }
        int width = Math.max(10, maxX - minX + 1);
        int height = Math.max(5, maxY - minY + 1);
        System.out.print("   ");
        for (int x = minX; x <= minX + width - 1; x++) {
            if (x < 10) {
                System.out.print(" " + x + "  ");
            } else {
                System.out.print(" " + x + " ");
            }
        }
        System.out.println();
        for (int y = minY; y <= minY + height - 1; y++) {
            System.out.print(y + " |");
            for (int x = minX; x <= minX + width - 1; x++) {
                String key = x + "," + y;
                MapNode node = nodeMap.get(key);
                if (node != null) {
                    char terrainChar;
                    switch (node.getTerrain()) {
                        case GRASS:
                            terrainChar = 'G';
                            break;
                        case MOUNTAIN:
                            terrainChar = 'M';
                            break;
                        case WATER:
                            terrainChar = 'W';
                            break;
                        default:
                            terrainChar = '?';
                    }
                    if (node.isFortPresent()) {
                        System.out.print(" " + terrainChar + "* ");
                    } else {
                        System.out.print(" " + terrainChar + "  ");
                    }
                } else {
                    System.out.print(" ?  ");
                }
            }
            System.out.println("|");
        }
        System.out.println("-------------------------");
        for (MapNode node : halfMap.getMapNodes()) {
            if (node.isFortPresent()) {
                System.out.println("\nFort location: (" + node.getX() + "," + node.getY() + "), " + 
                                  node.getTerrain().getName());
                break;
            }
        }
        printTerrainStatistics(halfMap.getMapNodes());
    }

    /**
     * Prints a full map representation to the console.
     * @param map The full map to print.
     */
    private void printFullMap(GameMap map) {
        if (map == null || map.getOwnHalfMap() == null || map.getOpponentHalfMap() == null) {
            System.out.println("Full map data not available.");
            return;
        }
        System.out.println("\nFull Map (" + (map.getMaxX() + 1) + "x" + (map.getMaxY() + 1) + 
                          ") - Orientation: " + map.getOrientation().getName());
        System.out.println("---------------------------------------------------");
        int width = map.getMaxX() + 1;
        int height = map.getMaxY() + 1;
        System.out.print("   ");
        for (int x = 0; x < width; x++) {
            if (x < 10) {
                System.out.print(" " + x + "  ");
            } else {
                System.out.print(" " + x + " ");
            }
        }
        System.out.println();
        for (int y = 0; y < height; y++) {
            if (y < 10) {
                System.out.print(y + " |");
            } else {
                System.out.print(y + "|");
            }
            for (int x = 0; x < width; x++) {
                try {
                    MapNode node = map.getNode(x, y);
                    if (node != null) {
                        char terrainChar;
                        switch (node.getTerrain()) {
                            case GRASS:
                                terrainChar = 'G';
                                break;
                            case MOUNTAIN:
                                terrainChar = 'M';
                                break;
                            case WATER:
                                terrainChar = 'W';
                                break;
                            default:
                                terrainChar = '?';
                        }
                        boolean isOwnHalf = false;
                        switch (map.getOrientation()) {
                            case UP_DOWN:
                                isOwnHalf = y < 5;
                                break;
                            case DOWN_UP:
                                isOwnHalf = y >= 5;
                                break;
                            case LEFT_RIGHT:
                                isOwnHalf = x < 10;
                                break;
                            case RIGHT_LEFT:
                                isOwnHalf = x >= 10;
                                break;
                        }
                        if (node.isFortPresent()) {
                            System.out.print(" " + terrainChar + (isOwnHalf ? "+" : "*") + " ");
                        } else {
                            if (isOwnHalf) {
                                System.out.print("[" + terrainChar + "] ");
                            } else {
                                System.out.print("(" + terrainChar + ") ");
                            }
                        }
                    } else {
                        System.out.print(" ?  ");
                    }
                } catch (Exception e) {
                    System.out.print(" ?  ");
                }
            }
            System.out.println("|");
        }
        System.out.println("---------------------------------------------------");
        MapNode ownFort = null;
        MapNode oppFort = null;
        for (MapNode node : map.getOwnHalfMap().getMapNodes()) {
            if (node.isFortPresent()) {
                ownFort = node;
                break;
            }
        }
        for (MapNode node : map.getOpponentHalfMap().getMapNodes()) {
            if (node.isFortPresent()) {
                oppFort = node;
                break;
            }
        }
        if (ownFort != null) {
            System.out.println("\nOwn Fort location: (" + ownFort.getX() + "," + ownFort.getY() + "), " + 
                              ownFort.getTerrain().getName());
        }
        if (oppFort != null) {
            System.out.println("Opponent Fort location: (" + oppFort.getX() + "," + oppFort.getY() + "), " + 
                              oppFort.getTerrain().getName());
        }
        System.out.println("\nLegend:");
        System.out.println("[G] - Own half Grass | (G) - Opponent half Grass");
        System.out.println("[M] - Own half Mountain | (M) - Opponent half Mountain");
        System.out.println("[W] - Own half Water | (W) - Opponent half Water");
        System.out.println("G+ - Own fort | G* - Opponent fort");
        java.util.List<MapNode> allNodes = new java.util.ArrayList<>();
        allNodes.addAll(map.getOwnHalfMap().getMapNodes());
        allNodes.addAll(map.getOpponentHalfMap().getMapNodes());
        System.out.println("\nFull Map Terrain Statistics:");
        printTerrainStatistics(allNodes);
    }

    /**
     * Prints terrain statistics for a list of map nodes.
     * @param nodes List of map nodes to analyze.
     */
    private void printTerrainStatistics(java.util.List<MapNode> nodes) {
        int grassCount = 0;
        int mountainCount = 0;
        int waterCount = 0;
        int fortCount = 0;
        for (MapNode node : nodes) {
            if (node.isFortPresent()) {
                fortCount++;
            }
            switch (node.getTerrain()) {
                case GRASS:
                    grassCount++;
                    break;
                case MOUNTAIN:
                    mountainCount++;
                    break;
                case WATER:
                    waterCount++;
                    break;
            }
        }
        int totalCells = nodes.size();
        System.out.println("\nTerrain Statistics:");
        System.out.println("------------------");
        System.out.println("Grass: " + grassCount + " fields (" + String.format("%.2f", (double) grassCount / totalCells * 100) + "%)");
        System.out.println("Mountains: " + mountainCount + " fields (" + String.format("%.2f", (double) mountainCount / totalCells * 100) + "%)");
        System.out.println("Water: " + waterCount + " fields (" + String.format("%.2f", (double) waterCount / totalCells * 100) + "%)");
        System.out.println("Forts: " + fortCount + " field" + (fortCount > 1 ? "s" : "") + " (" + 
                          String.format("%.2f", (double) fortCount / totalCells * 100) + "%)");
    }

    //for testing purposes, TDD
    public Object getGameState() {
        return gameState;
    }
    

}
