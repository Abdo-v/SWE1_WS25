package client.model.ai;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;

import client.model.GameState;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.PlayerHalfMap;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Tracks treasure discovery and provides traversal helpers for the treasure phase.
 */
class TreasureSeeker implements client.observer.util.Observer {

    // private static final Logger logger = LoggerFactory.getLogger(TreasureSeeker.class);
    private Optional<GameState> gameState;
    private boolean treasureFound = false;
    private final WayHelper wayHelper;

    public TreasureSeeker(WayHelper wayHelper) {
        this.gameState = Optional.empty();
        this.wayHelper = Objects.requireNonNull(wayHelper);
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
     * Returns the treasure node if found in the half map visited grass fields.
     *
     * @return the treasure node if found, otherwise empty
     */
    public Optional<MapNode> getTreasureNodeIfFound() {
        return gameState
                .flatMap(GameState::getMap)
                .map(GameMap::getOwnHalfMap)
                .map(PlayerHalfMap::getMapNodes)
                .flatMap(nodes -> nodes.stream().filter(MapNode::isTreasurePresent).findFirst());
    }

    public PlayerHalfMap getArrangedOwnHalfMap(MapNode currentPosition){
        return wayHelper.getArrangedOwnHalfMap(currentPosition);
    }

    @Override
    public void update(GameState gameState) {
        // logger.trace("TreasureSeeker received GameState update");
        this.gameState = Optional.of(Objects.requireNonNull(gameState, "gameState is required"));
        wayHelper.update(gameState);

        gameState.getTreasurePosition()
            .filter(pos -> !treasureFound)
            .ifPresent(pos -> treasureFound = true);
        
        //System.err.print(StaticColors.BLUE + "T" + StaticColors.RESET);
    }

}
