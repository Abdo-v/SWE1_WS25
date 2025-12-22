package client.model.ai;

import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.OwnToOppMapOrientation;
import client.model.mapper.PlayerHalfMap;

/**
 * Builds a snake (Y alternating) traversal ordering for half-maps.
 *
 * Package-private on purpose: extracted from WayHelper for SRP.
 */
final class HalfMapSnakeArranger {

    private HalfMapSnakeArranger() {
        // utility
    }

    static PlayerHalfMap arrangeOwnHalf(GameMap map, MapNode currentPosition) {
        if (map == null) {
            throw new IllegalArgumentException("GameMap cannot be null");
        }
        if (currentPosition == null) {
            throw new IllegalArgumentException("Current position cannot be null");
        }

        OwnToOppMapOrientation orientation = map.getOrientation();
        Bounds bounds = ownHalfBounds(orientation);

        boolean scanXLeftToRight;
        int startXIter;
        int endXIter;
        int iterXIncrement;

        if (currentPosition.getX() < bounds.xMidPointThreshold) {
            scanXLeftToRight = true;
            startXIter = bounds.minX;
            endXIter = bounds.maxX;
            iterXIncrement = 1;
        } else {
            scanXLeftToRight = false;
            startXIter = bounds.maxX;
            endXIter = bounds.minX;
            iterXIncrement = -1;
        }

        boolean currentYScanTopToBottom = currentPosition.getY() < bounds.yMidPointThreshold;

        PlayerHalfMap arranged = new PlayerHalfMap();
        for (int x = startXIter; (scanXLeftToRight ? x <= endXIter : x >= endXIter); x += iterXIncrement) {
            addColumn(map, arranged, x, bounds.minY, bounds.maxY, currentYScanTopToBottom);
            currentYScanTopToBottom = !currentYScanTopToBottom;
        }
        return arranged;
    }

    static PlayerHalfMap arrangeOpponentHalf(GameMap map, MapNode currentPosition) {
        if (map == null) {
            throw new IllegalArgumentException("GameMap cannot be null");
        }
        if (currentPosition == null) {
            throw new IllegalArgumentException("Current position cannot be null");
        }

        OwnToOppMapOrientation orientation = map.getOrientation();
        OpponentBounds bounds = opponentHalfBounds(orientation);

        boolean scanXLeftToRight = currentPosition.getX() < bounds.playerXThreshold;

        int startXIter;
        int endXIter;
        int iterXIncrement;
        if (scanXLeftToRight) {
            startXIter = bounds.minX;
            endXIter = bounds.maxX;
            iterXIncrement = 1;
        } else {
            startXIter = bounds.maxX;
            endXIter = bounds.minX;
            iterXIncrement = -1;
        }

        boolean currentYScanTopToBottom = currentPosition.getY() < bounds.playerYThreshold;

        PlayerHalfMap arranged = new PlayerHalfMap();
        for (int x = startXIter; (scanXLeftToRight ? x <= endXIter : x >= endXIter); x += iterXIncrement) {
            addColumn(map, arranged, x, bounds.minY, bounds.maxY, currentYScanTopToBottom);
            currentYScanTopToBottom = !currentYScanTopToBottom;
        }
        return arranged;
    }

    private static void addColumn(GameMap map, PlayerHalfMap halfMap, int x, int minY, int maxY, boolean topToBottom) {
        if (topToBottom) {
            for (int y = minY; y <= maxY; y++) {
                MapNode node = map.getNode(x, y);
                if (node != null) {
                    halfMap.addMapNode(node);
                }
            }
        } else {
            for (int y = maxY; y >= minY; y--) {
                MapNode node = map.getNode(x, y);
                if (node != null) {
                    halfMap.addMapNode(node);
                }
            }
        }
    }

    private static Bounds ownHalfBounds(OwnToOppMapOrientation orientation) {
        switch (orientation) {
            case UP_DOWN:
            case LEFT_RIGHT:
                return new Bounds(0, 9, 5, 0, 4, 3);
            case RIGHT_LEFT:
                return new Bounds(10, 19, 15, 0, 4, 3);
            case DOWN_UP:
                return new Bounds(0, 9, 5, 5, 9, 8);
            default:
                throw new IllegalArgumentException("Invalid orientation: " + orientation);
        }
    }

    private static OpponentBounds opponentHalfBounds(OwnToOppMapOrientation orientation) {
        switch (orientation) {
            case DOWN_UP:
                return new OpponentBounds(0, 9, 0, 4, 5, 8);
            case UP_DOWN:
                return new OpponentBounds(0, 9, 5, 9, 5, 3);
            case LEFT_RIGHT:
                return new OpponentBounds(10, 19, 0, 4, 5, 3);
            case RIGHT_LEFT:
                return new OpponentBounds(0, 9, 0, 4, 15, 3);
            default:
                throw new IllegalArgumentException("Invalid orientation: " + orientation);
        }
    }

    private record Bounds(int minX, int maxX, int xMidPointThreshold, int minY, int maxY, int yMidPointThreshold) {}

    private record OpponentBounds(int minX, int maxX, int minY, int maxY, int playerXThreshold, int playerYThreshold) {}
}
