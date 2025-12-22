package client.model.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Objects;
import java.util.Optional;
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

    private Optional<GameState> gameState;
    private boolean enemyFortFound = false;
    private WayHelper wayHelper;

    public FortSeeker(GameState gameState) {
        this.gameState = Optional.of(Objects.requireNonNull(gameState, "gameState must not be null"));
        this.wayHelper = new WayHelper(gameState);
    }

    public FortSeeker() {
        this.gameState = Optional.empty();
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

    public Optional<MapNode> getEnemyFortNodeIfFound() {
        return gameState
                .flatMap(GameState::getMap)
                .map(GameMap::getOpponentHalfMap)
                .map(PlayerHalfMap::getMapNodes)
                .flatMap(nodes -> nodes.stream().filter(MapNode::isFortPresent).findFirst());
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
        this.gameState = Optional.of(Objects.requireNonNull(gameState, "gameState must not be null"));
        wayHelper.update(gameState);

        gameState.getOpponentFortPosition()
            .filter(ignored -> !enemyFortFound)
            .ifPresent(ignored -> enemyFortFound = true);
    }
    // for testing purposes, TDD
	public Optional<GameState> getGameState() {
		return gameState;
	}



}
