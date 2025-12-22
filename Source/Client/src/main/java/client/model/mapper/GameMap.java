package client.model.mapper;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Predicate;

public class GameMap {
    private final MapEssentials map;
    private OwnToOppMapOrientation orientation;

    private static final String ORIENTATION_REQUIRED_MESSAGE = "Map orientation must be set";

    /**
     * Constructs a GameMap with the given parameters.
     * @param own The player's own half map.
     * @param opponent The opponent's half map.
     * @param orientation The orientation of the map.
     */
    public GameMap(ArrayList<MapNode> nodes, OwnToOppMapOrientation orientation) {
        this.orientation = Objects.requireNonNull(orientation, "orientation");

        int width;
        int height;
        if (orientation == OwnToOppMapOrientation.UP_DOWN || orientation == OwnToOppMapOrientation.DOWN_UP) {
            width = HalfMapDimensions.WIDTH;
            height = HalfMapDimensions.HEIGHT * 2;
        } else {
            width = HalfMapDimensions.WIDTH * 2;
            height = HalfMapDimensions.HEIGHT;
        }

        int maxX = width - 1;
        int maxY = height - 1;
        this.map = new MapEssentials(Objects.requireNonNull(nodes, "nodes"), maxX, maxY);
    }

    /**
     * Constructs a GameMap with the given parameters and dimensions.
     * @param own The player's own half map.
     * @param opponent The opponent's half map.
     * @param orientation The orientation of the map.
     * @param maxX The maximum X coordinate.
     * @param maxY The maximum Y coordinate.
     */
    public GameMap(ArrayList<MapNode> nodes, OwnToOppMapOrientation orientation, int maxX, int maxY) {
        this.orientation = Objects.requireNonNull(orientation, "orientation");
        this.map = new MapEssentials(Objects.requireNonNull(nodes, "nodes"), maxX, maxY);
    }

    /**
     * Default constructor for GameMap.
     */
    public GameMap() {
        this.map = new MapEssentials(new ArrayList<>(), 0, 0);
        this.orientation = null;
    }

    /**
     * Gets the player's own half map.
     * @return The player's own half map.
     */
    public PlayerHalfMap getOwnHalfMap() {
        if (map.size() == HalfMapDimensions.TOTAL_NODES) {
            return buildHalfMap(node -> true);
        }

        return buildHalfMap(ownHalfPredicate());
    }

    /**
     * Gets the opponent's half map.
     * @return The opponent's half map.
     */
    public PlayerHalfMap getOpponentHalfMap() {
        return buildHalfMap(ownHalfPredicate().negate());
    }


    /**
     * Gets the map orientation.
     * @return The map orientation.
     */
    public OwnToOppMapOrientation getOrientation() {
        return orientation;
    }

    /**
     * Sets the map orientation.
     * @param orientation The map orientation.
     */
    public void setOrientation(OwnToOppMapOrientation orientation) {
        this.orientation = orientation;
    }

    /**
     * Gets the maximum X coordinate.
     * @return The maximum X coordinate.
     */
    public int getMaxX() {
        return map.getMaxX();
    }

    /**
     * Sets the maximum X coordinate.
     * @param maxX The maximum X coordinate.
     */
    public void setMaxX(int maxX) {
        map.setMaxX(maxX);
    }

    /**
     * Gets the maximum Y coordinate.
     * @return The maximum Y coordinate.
     */
    public int getMaxY() {
        return map.getMaxY();
    }

    /**
     * Sets the maximum Y coordinate.
     * @param maxY The maximum Y coordinate.
     */
    public void setMaxY(int maxY) {
        map.setMaxY(maxY);
    }

    /**
     * Gets the total number of nodes in the map.
     * @return The total number of nodes in the map.
     */
    public int getContentSize() {
        return map.size();
    }

    /**
     * Gets the MapNode at the specified coordinates.
     * @param x X-coordinate.
     * @param y Y-coordinate.
     * @return The MapNode at the specified coordinates.
     * @throws IllegalArgumentException if coordinates are invalid.
     */
    public MapNode getNode(int x, int y) {
        for (MapNode node : map.getNodes()) {
            if (node.getX() == x && node.getY() == y) {
                return node;
            }
        }
        throw new IllegalArgumentException("Invalid coordinates: (" + x + ", " + y + "), not found in gameMap");
    }

    /**
     * Gets the half map MapNode that contains the player's own fort.
     * @return The MapNode containing the player's own fort.
     */
    public MapNode getOwnFortMapNode() {
        PlayerHalfMap ownHalfMap = getOwnHalfMap();
        return ownHalfMap.getFortNode().orElse(null);
    }

    /**
     * gets the map nodes of the game map.
     * @return An ArrayList of MapNode objects representing the game map nodes.
     */
    public ArrayList<MapNode> getGameMapNodes() {
        return map.getNodes();
    }
    /**
     * Checks if the given node is in the player's own half of the map.
     * The method uses the map's orientation to determine the player's half.
     * @param node The MapNode to check.
     * @return True if the node is in the player's own half, false otherwise.
     */
    public boolean isNodeInOwnHalf(MapNode node) {
        Objects.requireNonNull(node, "node");
        return ownHalfPredicate().test(node);
    }

    private PlayerHalfMap buildHalfMap(Predicate<MapNode> includeNode) {
        Objects.requireNonNull(includeNode, "includeNode");

        PlayerHalfMap halfMap = new PlayerHalfMap();
        for (MapNode node : map.getNodes()) {
            if (includeNode.test(node)) {
                halfMap.addMapNode(node);
            }
        }
        return halfMap;
    }

    private Predicate<MapNode> ownHalfPredicate() {
        if (orientation == null) {
            throw new IllegalStateException(ORIENTATION_REQUIRED_MESSAGE);
        }

        return switch (orientation) {
            case UP_DOWN -> node -> node.getY() <= HalfMapDimensions.HEIGHT - 1;
            case DOWN_UP -> node -> node.getY() >= HalfMapDimensions.HEIGHT;
            case LEFT_RIGHT -> node -> node.getX() <= HalfMapDimensions.WIDTH - 1;
            case RIGHT_LEFT -> node -> node.getX() >= HalfMapDimensions.WIDTH;
        };
    }
    /**
     * Returns a string representation of the GameMap.
     * @return A string representation of the GameMap.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GameMap{orientation=").append(orientation)
                    .append(", maxX=").append(getMaxX())
                    .append(", maxY=").append(getMaxY())
          .append(", nodes=[");
        for (MapNode node : map.getNodes()) {
            sb.append(node.toString()).append(", ");
        }
        sb.append("]}");
        return sb.toString();
    }

}
