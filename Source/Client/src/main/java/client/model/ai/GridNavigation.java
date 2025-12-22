package client.model.ai;

import client.model.Direction;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;

/**
 * Small grid navigation helpers.
 *
 * Package-private on purpose: extracted to keep AI classes focused.
 */
final class GridNavigation {

    private GridNavigation() {
        // utility
    }

    static MapNode getNodeInDirection(GameMap map, MapNode startNode, Direction direction) {
        if (map == null || startNode == null || direction == null) {
            return null;
        }

        int x = startNode.getX();
        int y = startNode.getY();

        switch (direction) {
            case UP: y--; break;
            case DOWN: y++; break;
            case LEFT: x--; break;
            case RIGHT: x++; break;
        }

        try {
            return map.getNode(x, y);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    static Direction getDirectionToNeighbor(MapNode current, MapNode neighbor) {
        if (current == null || neighbor == null) {
            throw new IllegalArgumentException("Current and neighbor nodes cannot be null");
        }

        int currentX = current.getX();
        int currentY = current.getY();
        int neighborX = neighbor.getX();
        int neighborY = neighbor.getY();

        // Only allow direct neighbors (one step in x or y, not both, and not diagonal)
        if ((Math.abs(currentX - neighborX) + Math.abs(currentY - neighborY)) != 1) {
            return null;
        }

        if (neighborX > currentX) return Direction.RIGHT;
        if (neighborX < currentX) return Direction.LEFT;
        if (neighborY > currentY) return Direction.DOWN;
        if (neighborY < currentY) return Direction.UP;
        return null;
    }
}
