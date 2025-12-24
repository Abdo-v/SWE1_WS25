package client.model.ai;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

import client.model.GameState;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.PlayerHalfMap;

public class FortSeeker implements client.observer.util.Observer {

    // private static final Logger logger = LoggerFactory.getLogger(FortSeeker.class);

    private Optional<GameState> gameState;
    private boolean enemyFortFound = false;
    private WayHelper wayHelper;

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

    public LinkedHashMap<MapNode, Boolean> getFilteredTraverseWay(MapNode enemyTruePosition){
        return wayHelper.getFilteredTraverseWay(enemyTruePosition);
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


}
