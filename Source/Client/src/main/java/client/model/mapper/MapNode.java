package client.model.mapper;

import java.util.Objects;

/**
 * A single grid cell on the game map.
 *
 * <p>This is used both as a lightweight DTO received from the server and as an internal reference
 * type in pathfinding/validation.
 */
public class MapNode {
    private int x;
    private int y;
    private Terrain terrain;
    private boolean fortPresent;
    private final boolean treasurePresent;

    public MapNode(int x, int y, Terrain terrain, boolean fortPresent, boolean treasurePresent) {
        this.x = x;
        this.y = y;
        this.terrain = Objects.requireNonNull(terrain, "terrain");
        this.fortPresent = fortPresent;
        this.treasurePresent = treasurePresent;
    }

    public MapNode() {
        this.x = -1;
        this.y = -1;
        this.terrain = Terrain.GRASS;
        this.fortPresent = false;
        this.treasurePresent = false;
        //System.err.println("DEBUG: MapNode default constructor called!");
        //new Throwable("MapNode default constructor stack trace").printStackTrace(System.err);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public void setTerrain(Terrain terrain) {
        this.terrain = Objects.requireNonNull(terrain, "terrain");
    }

    public boolean isFortPresent() {
        return fortPresent;
    }

    public void setFortPresent(boolean fortPresent) {
        this.fortPresent = fortPresent;
    }

    public boolean isTreasurePresent() {
        return treasurePresent;
    }

    public String toString() {
        return "MapNode{" +
                "x=" + x +
                ", y=" + y +
                ", terrain=" + terrain +
                ", fortPresent=" + fortPresent +
                ", treasurePresent=" + treasurePresent +
                '}';
    }

    public boolean equalsByCoordinates(MapNode other) {
        Objects.requireNonNull(other, "other");
        if (this == other) return true;
        return this.getX() == other.getX() && this.getY() == other.getY();
    }

    public boolean isWalkable() {
        return this.terrain != Terrain.WATER;
    }

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
        return Objects.hash(x, y, terrain, fortPresent, treasurePresent);
    }

}
