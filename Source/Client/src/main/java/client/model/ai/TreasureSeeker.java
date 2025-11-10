package client.model.ai;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import client.model.GameState;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.OwnToOppMapOrientation;
import client.model.mapper.PlayerHalfMap;
import client.model.mapper.Terrain;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

public class TreasureSeeker implements client.observer.util.Observer {

    // private static final Logger logger = LoggerFactory.getLogger(TreasureSeeker.class);
    private GameState gameState;
    private boolean treasureFound = false;

    /**
     * Constructs a TreasureSeeker with a non-null game state.
     * @param gameState The current game state.
     */
    public TreasureSeeker(GameState gameState) {
        this.gameState = gameState;
    }
    /**
     * Constructs a TreasureSeeker with no null game state.
     */
    public TreasureSeeker() {
        this.gameState = null;
    }
    /**
     * Returns a LinkedHashMap of grass nodes in the player's own half-map, initialized as unvisited.
     * The keys are MapNode objects representing grass nodes, and the values are Booleans indicating whether the node has been visited.
     * 
     * @return A LinkedHashMap of grass nodes with their visited status.
     */
    public LinkedHashMap<MapNode, Boolean> getTraverseWay() {
        // logger.debug("Generating traversal way for own half-map");
        //System.out.println("TreasureSeeker: getTreverseWay: current node: " + gameState.getPlayers().get(0).getCurrentPosition().toString());// for log
        ArrayList<MapNode> grassNodes = new ArrayList<>();
        if (gameState != null && gameState.getMap() != null) {
            //PlayerHalfMap ownHalfMap = gameState.getMap().getArrangedOwnHalfMap(gameState.getCurrentPlayerState().getCurrentPosition());
            PlayerHalfMap ownHalfMap = getArrangedOwnHalfMap(gameState.getOwnFortPosition());
            if (ownHalfMap != null && ownHalfMap.getMapNodes() != null) {
                grassNodes = ownHalfMap.getMapNodes().stream()
                        .filter(node -> node.getTerrain() == Terrain.GRASS)
                        .collect(Collectors.toCollection(ArrayList::new));
                // logger.debug("Found {} grass nodes in arranged own half-map", grassNodes.size());
            } else {
                // logger.warn("Own half-map or its nodes are null");
            }
            //System.out.println("initial arranged own half: "+ grassNodes.toString());//for log
        }
        else{
            if(gameState == null) {
                // logger.error("GameState is null, cannot get traversal way");
                System.err.println("TreasureSeeker: GameState is null, cannot get traversal way.");
                new Throwable("TreasureSeeker: GameState is null, cannot get traversal way.").printStackTrace();
            } else if (gameState.getMap() == null) {
                // logger.error("GameMap is null, cannot get traversal way");
                System.err.println("TreasureSeeker: GameMap is null, cannot get traversal way.");
            } else {
                // logger.warn("No grass nodes found in the specified half-map");
                System.err.println("TreasureSeeker: No grass nodes found in the specified half-map.");
            }
        }
        LinkedHashMap<MapNode, Boolean> grassTraversal = new LinkedHashMap<>();
        for (MapNode node : grassNodes) {
            // Initialize all grass nodes as unvisited
            grassTraversal.put(node, false);
        }
        // logger.debug("Initialized traversal way with {} grass nodes (all unvisited)", grassTraversal.size());
        //System.out.println("arranged half: "+ grassTraversal.toString());
        return grassTraversal;
    }

    /**
     * returns the treasure node if found in the half map visited grass fields
     * @param halfMapVisitedGrassFields
     * @return the treasure node if found, otherwise null
     */
    public MapNode getTreasureNodeIfFound() {
        // logger.trace("Searching for treasure in own half-map");
        MapNode res = null;
        
        if (gameState == null || gameState.getMap() == null) {
            // logger.warn("Cannot search for treasure - GameState or map is null");
            return null;
        }
        
        for (MapNode node : gameState.getMap().getOwnHalfMap().getMapNodes()) {
            if (node.isTreasurePresent()) {
                res = node;
                // logger.info("Treasure found at position: {}", res.printCoordinates());
                break;
            }
        }
        
        if(res != null) {
            // logger.debug("Treasure confirmed at: {}, current position: {}", 
            //             res.printCoordinates(), 
            //             gameState.getCurrentPlayerState().getCurrentPosition().printCoordinates());
            //System.out.println("Treasure found at: " + res.toString() + ", current position: " + gameState.getCurrentPlayerState().getCurrentPosition().toString());// for log
        } else {
            // logger.trace("No treasure found in own half-map");
        }
        return res; // placeholder
    }

    /**
     * Returns the arranged own half map based on the current position of the player.
     * The arrangement is done in a Y-snake traversal pattern, starting from the player's current position.
     * 
     * @param currentPosition The current position of the player.
     * @return The arranged PlayerHalfMap containing nodes in Y-snake order.
     */
    public PlayerHalfMap getArrangedOwnHalfMap(MapNode currentPosition){
        // logger.debug("Arranging own half-map in Y-snake pattern from position: {}", 
        //             currentPosition != null ? currentPosition.printCoordinates() : "null");
        
        GameMap currentMap = gameState.getMap();
        PlayerHalfMap arrangedOwnHalfMap = new PlayerHalfMap();
        if (currentPosition == null) {
            // logger.error("Current position cannot be null for map arrangement");
            throw new IllegalArgumentException("Current position cannot be null");
        }

        int minScanX, maxScanX, xMidPointThreshold;
        int minScanY, maxScanY, yMidPointThreshold;
        OwnToOppMapOrientation orientation = currentMap.getOrientation();
        
        // logger.trace("Map orientation for arrangement: {}", orientation);
        
        // Determine the boundaries and midpoints for the player's own half-map
        switch (orientation) {
            case UP_DOWN:
            case LEFT_RIGHT:
                // Own half is X: 0-9, Y: 0-4
                minScanX = 0; maxScanX = 9; xMidPointThreshold = 5; // currentPosition.getX() < 5 means left part
                minScanY = 0; maxScanY = 4; yMidPointThreshold = 3; // currentPosition.getY() < 3 means top part
                break;
            case RIGHT_LEFT:
                // Own half is X: 10-19, Y: 0-4
                minScanX = 10; maxScanX = 19; xMidPointThreshold = 15; // currentPosition.getX() < 15 means "left" part of this half
                minScanY = 0; maxScanY = 4; yMidPointThreshold = 3;  // currentPosition.getY() < 3 means top part
                break;
            case DOWN_UP:
                // Own half is X: 0-9, Y: 5-9
                minScanX = 0; maxScanX = 9; xMidPointThreshold = 5;  // currentPosition.getX() < 5 means left part
                minScanY = 5; maxScanY = 9; yMidPointThreshold = 8;  // currentPosition.getY() < 8 means "top" part of this half
                break;
            default:
                // logger.error("Invalid map orientation: {}", orientation);
                throw new IllegalArgumentException("Invalid orientation: " + orientation);
        }

        // logger.trace("Scan boundaries - X: {}-{} (threshold: {}), Y: {}-{} (threshold: {})", 
        //             minScanX, maxScanX, xMidPointThreshold, minScanY, maxScanY, yMidPointThreshold);

        // Determine X iteration direction (outer loop)
        boolean scanXLeftToRight;
        int currentIterX, endIterX, iterXIncrement;

        if (currentPosition.getX() < xMidPointThreshold) { // Player is in the "left/top" part of X-range for this half
            scanXLeftToRight = true;
            currentIterX = minScanX;
            endIterX = maxScanX;
            iterXIncrement = 1;
        } else { // Player is in the "right/bottom" part of X-range for this half
            scanXLeftToRight = false;
            currentIterX = maxScanX;
            endIterX = minScanX;
            iterXIncrement = -1;
        }

        // Determine initial Y iteration direction for the first X column (inner loop)
        boolean currentYScanTopToBottom;
        if (currentPosition.getY() < yMidPointThreshold) { // Player is in the "top" part of Y-range for this half
            currentYScanTopToBottom = true;
        } else { // Player is in the "bottom" part of Y-range for this half
            currentYScanTopToBottom = false;
        }

        // logger.trace("Y-snake traversal configuration - X: {} to {} (increment: {}), initial Y direction: {}", 
        //             currentIterX, endIterX, iterXIncrement, currentYScanTopToBottom ? "top-to-bottom" : "bottom-to-top");

        int nodesAdded = 0;
        // Y-Snake traversal
        for (int x = currentIterX; (scanXLeftToRight ? x <= endIterX : x >= endIterX); x += iterXIncrement) {
            if (currentYScanTopToBottom) {
                // Inner loop for Y: Top to Bottom for this column
                for (int y = minScanY; y <= maxScanY; y++) {
                    MapNode node = currentMap.getNode(x, y);
                    if (node != null) {
                        arrangedOwnHalfMap.addMapNode(node);
                        nodesAdded++;
                    }
                }
            } else {
                // Inner loop for Y: Bottom to Top for this column
                for (int y = maxScanY; y >= minScanY; y--) {
                    MapNode node = currentMap.getNode(x, y);
                    if (node != null) {
                        arrangedOwnHalfMap.addMapNode(node);
                        nodesAdded++;
                    }
                }
            }
            currentYScanTopToBottom = !currentYScanTopToBottom; // Flip Y scan direction for the next X column
        }
        
        // logger.debug("Y-snake arrangement completed - added {} nodes to own half-map", nodesAdded);
        return arrangedOwnHalfMap;
    }

    @Override
    public void update(GameState gameState) {
        // logger.trace("TreasureSeeker received GameState update");
        this.gameState = gameState;
        
        if (gameState.getTreasurePosition() != null && !treasureFound) {
            treasureFound = true;
            // logger.info("Treasure discovered at position: {}", gameState.getTreasurePosition().printCoordinates());
        }
        
        //System.err.print(StaticColors.BLUE + "T" + StaticColors.RESET);
    }

    // for testing, TDD
    public Object getGameState() {
        return gameState;
    }

}
