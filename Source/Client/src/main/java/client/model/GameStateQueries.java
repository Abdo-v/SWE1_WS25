package client.model;

import client.model.mapper.GameMap;
import client.model.mapper.MapNode;

/**
 * Read-only queries derived from {@link GameState} and its {@link GameMap}.
 */
final class GameStateQueries {

    private static final int OWN_HALF_MAX_Y_UP_DOWN = 4;
    private static final int OWN_HALF_MIN_Y_DOWN_UP = 5;
    private static final int OWN_HALF_MAX_X_LEFT_RIGHT = 9;
    private static final int OWN_HALF_MIN_X_RIGHT_LEFT = 10;

    MapNode getOwnFortPosition(GameMap map) {
        if (map == null || map.getOwnHalfMap() == null) {
            return null;
        }
        for (MapNode node : map.getOwnHalfMap().getMapNodes()) {
            if (node.isFortPresent()) {
                return node;
            }
        }
        return null;
    }

    MapNode getEnemyFortPosition(GameMap map) {
        if (map == null || map.getOpponentHalfMap() == null) {
            return null;
        }
        for (MapNode node : map.getOpponentHalfMap().getMapNodes()) {
            if (node.isFortPresent()) {
                return node;
            }
        }
        return null;
    }

    MapNode getEnemyCurrentPosition(GameState state) {
        if (state == null || state.getPlayers() == null || state.getPlayers().size() < 2) {
            return null;
        }
        return state.getPlayers().get(1).getCurrentPosition();
    }

    PlayerState getEnemyPlayerState(GameState state) {
        if (state == null || state.getPlayers() == null || state.getPlayers().size() < 2) {
            return null;
        }
        return state.getPlayers().get(1);
    }

    boolean isPlayerInOwnHalfMap(GameMap map, MapNode currentNode) {
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
