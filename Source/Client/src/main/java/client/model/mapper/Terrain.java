package client.model.mapper;

/** Terrain types used for movement and validation. */
public enum Terrain {
    GRASS("Grass"),
    MOUNTAIN("Mountain"),
    WATER("Water");

    //field name to be used in outputs and logs
    private final String name;
    Terrain(String name) {
        this.name = name;
    }

}
