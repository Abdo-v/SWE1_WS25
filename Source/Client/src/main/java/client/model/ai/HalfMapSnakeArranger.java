package client.model.ai;

import client.model.mapper.GameMap;
import client.model.mapper.HalfMapDimensions;
import java.util.Objects;
import client.model.mapper.MapNode;
import client.model.mapper.OwnToOppMapOrientation;
import client.model.mapper.PlayerHalfMap;

/**
 * Builds a snake (Y alternating) traversal ordering for half-maps.
 *
 * Package-private on purpose: extracted from WayHelper for SRP.
 */
final class HalfMapSnakeArranger {

    private static final int HALF_WIDTH = HalfMapDimensions.WIDTH;
    private static final int HALF_HEIGHT = HalfMapDimensions.HEIGHT;

    // Preserves the original "midpoint" behavior:
    // for a half with HEIGHT=5 -> threshold = minY + 3 (splits rows 0-2 vs 3-4).
    private static final int HALF_Y_MIDPOINT_OFFSET = HALF_HEIGHT - 2;

    private HalfMapSnakeArranger() {
        // utility
    }

    static PlayerHalfMap arrangeOwnHalf(GameMap map, MapNode currentPosition) {
        if (Objects.isNull(map)) {
            throw new IllegalArgumentException("GameMap cannot be null");
        }
        if (Objects.isNull(currentPosition)) {
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
        if (Objects.isNull(map)) {
            throw new IllegalArgumentException("GameMap cannot be null");
        }
        if (Objects.isNull(currentPosition)) {
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
                halfMap.addMapNode(node);
            }
        } else {
            for (int y = maxY; y >= minY; y--) {
                MapNode node = map.getNode(x, y);
                halfMap.addMapNode(node);
            }
        }
    }

    private static Bounds ownHalfBounds(OwnToOppMapOrientation orientation) {
        int minX;
        int maxX;
        int minY;
        int maxY;

        // Note: For vertical orientations, width is HALF_WIDTH and height is 2*HALF_HEIGHT.
        // For horizontal orientations, width is 2*HALF_WIDTH and height is HALF_HEIGHT.
        switch (orientation) {
            case UP_DOWN, LEFT_RIGHT -> {
                minX = 0;
                maxX = HALF_WIDTH - 1;
                minY = 0;
                maxY = HALF_HEIGHT - 1;
            }
            case RIGHT_LEFT -> {
                minX = HALF_WIDTH;
                maxX = (HALF_WIDTH * 2) - 1;
                minY = 0;
                maxY = HALF_HEIGHT - 1;
            }
            case DOWN_UP -> {
                minX = 0;
                maxX = HALF_WIDTH - 1;
                minY = HALF_HEIGHT;
                maxY = (HALF_HEIGHT * 2) - 1;
            }
            default -> throw new IllegalArgumentException("Invalid orientation: " + orientation);
        }

        int xMidPointThreshold = minX + (HALF_WIDTH / 2);
        int yMidPointThreshold = minY + HALF_Y_MIDPOINT_OFFSET;
        return new Bounds(minX, maxX, xMidPointThreshold, minY, maxY, yMidPointThreshold);
    }

    private static OpponentBounds opponentHalfBounds(OwnToOppMapOrientation orientation) {
        Bounds own = ownHalfBounds(orientation);

        int minX;
        int maxX;
        int minY;
        int maxY;

        switch (orientation) {
            case DOWN_UP -> {
                // opponent is top half
                minX = 0;
                maxX = HALF_WIDTH - 1;
                minY = 0;
                maxY = HALF_HEIGHT - 1;
            }
            case UP_DOWN -> {
                // opponent is bottom half
                minX = 0;
                maxX = HALF_WIDTH - 1;
                minY = HALF_HEIGHT;
                maxY = (HALF_HEIGHT * 2) - 1;
            }
            case LEFT_RIGHT -> {
                // opponent is right half
                minX = HALF_WIDTH;
                maxX = (HALF_WIDTH * 2) - 1;
                minY = 0;
                maxY = HALF_HEIGHT - 1;
            }
            case RIGHT_LEFT -> {
                // opponent is left half
                minX = 0;
                maxX = HALF_WIDTH - 1;
                minY = 0;
                maxY = HALF_HEIGHT - 1;
            }
            default -> throw new IllegalArgumentException("Invalid orientation: " + orientation);
        }

        return new OpponentBounds(minX, maxX, minY, maxY, own.xMidPointThreshold, own.yMidPointThreshold);
    }

    private record Bounds(int minX, int maxX, int xMidPointThreshold, int minY, int maxY, int yMidPointThreshold) {}

    private record OpponentBounds(int minX, int maxX, int minY, int maxY, int playerXThreshold, int playerYThreshold) {}
}
