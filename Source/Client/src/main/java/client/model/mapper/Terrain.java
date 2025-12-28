package client.model.mapper;

public enum Terrain {
    GRASS("Grass"),
    MOUNTAIN("Mountain"),
    WATER("Water");

    //field name to be used in outputs and logs
    private final String name;

    /**
     * Constructs a Terrain enum with the given name.
     * @param name The name of the terrain.
     */
    Terrain(String name) {
        this.name = name;
    }

}
