package client.model.mapper.validator;

final class HalfMapBounds {
    private final int maxX;
    private final int maxY;

    HalfMapBounds(int maxX, int maxY) {
        this.maxX = maxX;
        this.maxY = maxY;
    }

    int maxX() {
        return maxX;
    }

    int maxY() {
        return maxY;
    }
}
