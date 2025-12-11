package client.model.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

import client.model.GameState;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.PlayerHalfMap;
import client.model.mapper.Terrain;
import client.model.mapper.OwnToOppMapOrientation;

public class FortSeeker implements client.observer.util.Observer {

    // private static final Logger logger = LoggerFactory.getLogger(FortSeeker.class);

    private GameState gameState;
    private boolean enemyFortFound = false;
    private WayHelper wayHelper;

    public FortSeeker(GameState gameState) {
        this.gameState = gameState;
        this.wayHelper = new WayHelper(gameState);
    }

    public FortSeeker() {
        this.gameState = null;
        this.wayHelper = new WayHelper();
    }
    /**
     * Returns a LinkedHashMap of grass nodes in the opponent's half-map, initialized as unvisited.
     * The keys are MapNode objects representing grass nodes, and the values are Booleans indicating if the node has been visited.
     * @return A LinkedHashMap of grass nodes with their visited status.
     */
    public LinkedHashMap<MapNode, Boolean> getTraverseWay(){
        return wayHelper.getTraverseWayForOpponentHalf();
    }

    public MapNode getEnemyFortNodeIfFound() {
        // logger.trace("Searching for enemy fort in opponent half-map");
        MapNode res = null;
        if (gameState == null || gameState.getMap() == null) {
            // logger.warn("Cannot search for enemy fort - GameState or map is null");
            return null;
        }
        if (gameState.getCurrentPlayerState() == null) {
            throw new IllegalArgumentException("Current player state cannot be null");
        }
        for (MapNode node : gameState.getMap().getOpponentHalfMap().getMapNodes()) {
            if (node.isFortPresent()) {
                res = node;
                // logger.info("Enemy fort found at position: {}", res.printCoordinates());
                break;
            }
        }
        if(res != null) {
            // logger.debug("Enemy fort confirmed at: {}, current position: {}", 
            //             res.printCoordinates(),
            //             gameState.getCurrentPlayerState().getCurrentPosition().printCoordinates());
        } else {
            // logger.trace("No enemy fort found in opponent half-map");
        }
        return res;
    }

    public LinkedHashMap<MapNode, Boolean> getTraverseWay(MapNode currentPosition){
        return wayHelper.getTraverseWayForOpponentHalf(currentPosition);
    }

    public LinkedHashMap<MapNode, Boolean> getFilteredTraverseWay(MapNode enemyTruePosition){
        return wayHelper.getFilteredTraverseWay(enemyTruePosition);
    }

    /**
     * gets the opponent half map arranged from the corner nearest to current (start) position in a Y-snake pattern.
     * The outer loop iterates X, and the inner loop Y, with Y direction alternating.
     * @param currentPosition the current position of the player (in their own half).
     * @return the arranged opponent half map.
     */
    public PlayerHalfMap getArrangedOpponentHalfMap(MapNode currentPosition){
        return wayHelper.getArrangedOpponentHalfMap(currentPosition);
    }

    @Override
    public void update(GameState gameState) {
        // logger.trace("FortSeeker received GameState update");
        this.gameState = gameState;
        wayHelper.update(gameState);
        
        if (gameState.getOpponentFortPosition() != null && !enemyFortFound) {
            enemyFortFound = true;
            // logger.info("Enemy fort discovered at position: {}", gameState.getOpponentFortPosition().printCoordinates());
        }
    }
    // for testing purposes, TDD
	public Object getGameState() {
		return gameState;
	}



}
