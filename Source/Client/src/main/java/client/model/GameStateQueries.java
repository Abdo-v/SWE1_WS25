package client.model;

import client.model.mapper.GameMap;
import client.model.mapper.MapNode;

import java.util.Objects;
import java.util.Optional;

/**
 * Read-only queries derived from {@link GameState} and its {@link GameMap}.
 */
final class GameStateQueries {

    private static final int OWN_HALF_MAX_Y_UP_DOWN = 4;
    private static final int OWN_HALF_MIN_Y_DOWN_UP = 5;
    private static final int OWN_HALF_MAX_X_LEFT_RIGHT = 9;
    private static final int OWN_HALF_MIN_X_RIGHT_LEFT = 10;

    Optional<MapNode> getOwnFortPosition(GameMap map) {
        Objects.requireNonNull(map, "map");
        for (MapNode node : map.getOwnHalfMap().getMapNodes()) {
            if (node.isFortPresent()) {
                return Optional.of(node);
            }
        }
        return Optional.empty();
    }

    Optional<MapNode> getEnemyFortPosition(GameMap map) {
        Objects.requireNonNull(map, "map");
        for (MapNode node : map.getOpponentHalfMap().getMapNodes()) {
            if (node.isFortPresent()) {
                return Optional.of(node);
            }
        }
        return Optional.empty();
    }

    Optional<MapNode> getEnemyCurrentPosition(GameState state) {
        Objects.requireNonNull(state, "state");
        if (state.getPlayers().size() < 2) {
            return Optional.empty();
        }
        return state.getPlayers().get(1).getCurrentPosition();
    }

    Optional<PlayerState> getEnemyPlayerState(GameState state) {
        Objects.requireNonNull(state, "state");
        if (state.getPlayers().size() < 2) {
            return Optional.empty();
        }
        return Optional.of(state.getPlayers().get(1));
    }

    boolean isPlayerInOwnHalfMap(GameMap map, MapNode currentNode) {
        Objects.requireNonNull(map, "map");
        Objects.requireNonNull(currentNode, "currentNode");
        switch (map.getOrientation()) {
            case UP_DOWN:
                return currentNode.getY() <= OWN_HALF_MAX_Y_UP_DOWN;
            case DOWN_UP:
                return currentNode.getY() >= OWN_HALF_MIN_Y_DOWN_UP;
            case LEFT_RIGHT:
                return currentNode.getX() <= OWN_HALF_MAX_X_LEFT_RIGHT;
            case RIGHT_LEFT:
                return currentNode.getX() >= OWN_HALF_MIN_X_RIGHT_LEFT;
            default:
                return false;
        }
    }
}
