package client.model.ai;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;

import client.model.GameState;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.PlayerHalfMap;

/**
 * Tracks enemy fort discovery and provides traversal helpers for the fort phase.
 */
class FortSeeker implements client.observer.util.Observer {

    private Optional<GameState> gameState;
    private boolean enemyFortFound = false;
    private final WayHelper wayHelper;

    public FortSeeker(WayHelper wayHelper) {
        this.gameState = Optional.empty();
        this.wayHelper = Objects.requireNonNull(wayHelper);
    }
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
        this.gameState = Optional.of(Objects.requireNonNull(gameState, "gameState is required"));
        wayHelper.update(gameState);

        gameState.getOpponentFortPosition()
            .filter(ignored -> !enemyFortFound)
            .ifPresent(ignored -> enemyFortFound = true);
    }


}
