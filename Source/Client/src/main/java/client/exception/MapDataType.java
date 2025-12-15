package client.exception;

public enum MapDataType {
    GAME_STATE("GameState"),
    HALF_MAP("HalfMap"),
    FULL_MAP("FullMap"),
    UNKNOWN("Unknown");

    private final String label;

    MapDataType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
