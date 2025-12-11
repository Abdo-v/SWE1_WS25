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
    private WayHelper wayHelper;

    /**
     * Constructs a TreasureSeeker with a non-null game state.
     * @param gameState The current game state.
     */
    public TreasureSeeker(GameState gameState) {
        this.gameState = gameState;
        this.wayHelper = new WayHelper(gameState);
    }
    /**
     * Constructs a TreasureSeeker with no null game state.
     */
    public TreasureSeeker() {
        this.gameState = null;
        this.wayHelper = new WayHelper();
    }
    /**
     * Returns a LinkedHashMap of grass nodes in the player's own half-map, initialized as unvisited.
     * The keys are MapNode objects representing grass nodes, and the values are Booleans indicating whether the node has been visited.
     * 
     * @return A LinkedHashMap of grass nodes with their visited status.
     */
    public LinkedHashMap<MapNode, Boolean> getTraverseWay() {
        return wayHelper.getTraverseWayForOwnHalf();
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
        return wayHelper.getArrangedOwnHalfMap(currentPosition);
    }

    @Override
    public void update(GameState gameState) {
        // logger.trace("TreasureSeeker received GameState update");
        this.gameState = gameState;
        wayHelper.update(gameState);
        
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
