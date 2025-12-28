package client.model.mapper;

/**
 * Orientation describing how the local player's half-map is placed relative to the opponent's.
 *
 * <p>This is used to split a full map into "own" vs "opponent" views.
 */
public enum OwnToOppMapOrientation {
    UP_DOWN("Up Down"),
    LEFT_RIGHT("Left Right"),
    RIGHT_LEFT("Right Left"),
    DOWN_UP("Down Up");

    private final String name;
    OwnToOppMapOrientation(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
}
