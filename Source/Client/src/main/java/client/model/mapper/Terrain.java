package client.model.mapper;

public enum Terrain {
    GRASS("Grass"),
    MOUNTAIN("Mountain"),
    WATER("Water");

    private final String name;

    /**
     * Constructs a Terrain enum with the given name.
     * @param name The name of the terrain.
     */
    Terrain(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the terrain.
     * @return The name of the terrain.
     */
    public String getName() {
        return name;
    }
}
