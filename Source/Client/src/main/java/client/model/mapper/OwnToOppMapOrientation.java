package client.model.mapper;

public enum OwnToOppMapOrientation {
    UP_DOWN("Up Down"),
    LEFT_RIGHT("Left Right"),
    RIGHT_LEFT("Right Left"),
    DOWN_UP("Down Up");

    private final String name;

    /**
     * Constructs an OwnToOppMapOrientation with the given name.
     * @param name The name of the orientation.
     */
    OwnToOppMapOrientation(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the orientation.
     * @return The name of the orientation.
     */
    public String getName() {
        return name;
    }
}
