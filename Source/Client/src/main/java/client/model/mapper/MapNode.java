package client.model.mapper;

public class MapNode {
    private int x;
    private int y;
    private Terrain terrain;
    private boolean fortPresent;
    private boolean treasurePresent;

    /**
     * Constructs a MapNode with the given parameters.
     * @param x The X coordinate of the node.
     * @param y The Y coordinate of the node.
     * @param terrain The terrain type of the node.
     * @param fortPresent Whether a fort is present on the node.
     * @param treasurePresent Whether a treasure is present on the node.
     */
    public MapNode(int x, int y, Terrain terrain, boolean fortPresent, boolean treasurePresent) {
        this.x = x;
        this.y = y;
        this.terrain = terrain;
        this.fortPresent = fortPresent;
        this.treasurePresent = treasurePresent;
    }

    /**
     * Default constructor for MapNode.
     */
    public MapNode() {
        this.x = -1;
        this.y = -1;
        this.terrain = Terrain.GRASS;
        this.fortPresent = false;
        this.treasurePresent = false;
        //System.err.println("DEBUG: MapNode default constructor called!");
        //new Throwable("MapNode default constructor stack trace").printStackTrace(System.err);
    }

    /**
     * Gets the X coordinate of the node.
     * @return The X coordinate of the node.
     */
    public int getX() {
        return x;
    }

    /**
     * Sets the X coordinate of the node.
     * @param x The X coordinate of the node.
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Gets the Y coordinate of the node.
     * @return The Y coordinate of the node.
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the Y coordinate of the node.
     * @param y The Y coordinate of the node.
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Gets the terrain type of the node.
     * @return The terrain type of the node.
     */
    public Terrain getTerrain() {
        return terrain;
    }

    /**
     * Sets the terrain type of the node.
     * @param terrain The terrain type of the node.
     */
    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }

    /**
     * Checks if a fort is present on the node.
     * @return true if a fort is present on the node, false otherwise.
     */
    public boolean isFortPresent() {
        return fortPresent;
    }

    /**
     * Sets whether a fort is present on the node.
     * @param fortPresent Whether a fort is present on the node.
     */
    public void setFortPresent(boolean fortPresent) {
        this.fortPresent = fortPresent;
    }

    /**
     * Checks if a treasure is present on the node.
     * @return true if a treasure is present on the node, false otherwise.
     */
    public boolean isTreasurePresent() {
        return treasurePresent;
    }
    
    /**
     * Sets whether a treasure is present on the node.
     * @param treasurePresent Whether a treasure is present on the node.
     */

    public void setTreasurePresent(boolean treasurePresent) {
        this.treasurePresent = treasurePresent;
    }
    /**
     * Returns a string representation of the MapNode.
     * @return A string representation of the MapNode.
     */
    public String toString() {
        return "MapNode{" +
                "x=" + x +
                ", y=" + y +
                ", terrain=" + terrain +
                ", fortPresent=" + fortPresent +
                ", treasurePresent=" + treasurePresent +
                '}';
    }

    /**
     * Checks if this MapNode has the same coordinates as another MapNode.
     * @param other The other MapNode to compare to.
     * @return true if the coordinates are the same, false otherwise.
     */
    public boolean equalsByCoordinates(MapNode other) {
        if (other == null) throw new IllegalArgumentException("Other MapNode cannot be null");
        if (this == other) return true;
        return this.getX() == other.getX() && this.getY() == other.getY();
    }

    /**
     * Localizes the MapNode to half maps.
     * mainly used in way finding.
     * @return A new MapNode with localized coordinates.
     */

    public MapNode localize(){
        int localizedX = x % 10; // Assuming a grid size of 10 for localization
        int localizedY = y % 10; // Assuming a grid size of 10 for localization
        return new MapNode(localizedX, localizedY, terrain, fortPresent, treasurePresent);
    }

    /**
     * A node is walkable if its terrain is not WATER.
     * @return true if the node is walkable, false otherwise.
     */
    public boolean isWalkable() {
        return this.terrain != Terrain.WATER;
    }

    /**
     * Checks if this MapNode has the same coordinates as another MapNode.
     * @param x The X coordinate to compare.
     * @param y The Y coordinate to compare.
     * @return true if the coordinates are the same, false otherwise.
     */
    public String printCoordinates() {
        return "Coordinates: (" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapNode)) return false;
        MapNode mapNode = (MapNode) o;
        return x == mapNode.x && y == mapNode.y && terrain == mapNode.terrain && fortPresent == mapNode.fortPresent && treasurePresent == mapNode.treasurePresent;
    }
    @Override
    public int hashCode() {
        int result = Integer.hashCode(x);
        result = 31 * result + Integer.hashCode(y);
        result = 31 * result + (terrain != null ? terrain.hashCode() : 0);
        result = 31 * result + Boolean.hashCode(fortPresent);
        result = 31 * result + Boolean.hashCode(treasurePresent);
        return result;
        // if performance not important:
        // return Objects.hash(x, y, terrain, fortPresent, treasurePresent);
    }

}
