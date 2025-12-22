package client.model.ai;

import client.model.Direction;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;

import java.util.Objects;
import java.util.Optional;

/**
 * Small grid navigation helpers.
 *
 * Package-private on purpose: extracted to keep AI classes focused.
 */
final class GridNavigation {

    private GridNavigation() {
        // utility
    }

    static Optional<MapNode> getNodeInDirection(GameMap map, MapNode startNode, Direction direction) {
        Objects.requireNonNull(map, "map must not be null");
        Objects.requireNonNull(startNode, "startNode must not be null");
        Objects.requireNonNull(direction, "direction must not be null");

        int x = startNode.getX();
        int y = startNode.getY();

        switch (direction) {
            case UP: y--; break;
            case DOWN: y++; break;
            case LEFT: x--; break;
            case RIGHT: x++; break;
        }

        try {
            return Optional.of(map.getNode(x, y));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    static Optional<Direction> getDirectionToNeighbor(MapNode current, MapNode neighbor) {
        Objects.requireNonNull(current, "current must not be null");
        Objects.requireNonNull(neighbor, "neighbor must not be null");

        int currentX = current.getX();
        int currentY = current.getY();
        int neighborX = neighbor.getX();
        int neighborY = neighbor.getY();

        // Only allow direct neighbors (one step in x or y, not both, and not diagonal)
        if ((Math.abs(currentX - neighborX) + Math.abs(currentY - neighborY)) != 1) {
            return Optional.empty();
        }

        if (neighborX > currentX) return Optional.of(Direction.RIGHT);
        if (neighborX < currentX) return Optional.of(Direction.LEFT);
        if (neighborY > currentY) return Optional.of(Direction.DOWN);
        if (neighborY < currentY) return Optional.of(Direction.UP);
        return Optional.empty();
    }
}
