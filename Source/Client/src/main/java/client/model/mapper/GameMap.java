package client.model.mapper;
import java.util.ArrayList;

public class GameMap {
    private ArrayList<MapNode> gameMapNodes;
    private OwnToOppMapOrientation orientation;
    private int maxX = 0;
    private int maxY = 0;

    /**
     * Constructs a GameMap with the given parameters.
     * @param own The player's own half map.
     * @param opponent The opponent's half map.
     * @param orientation The orientation of the map.
     */
    public GameMap(ArrayList<MapNode> nodes, OwnToOppMapOrientation orientation) {
        this.gameMapNodes = nodes;
        this.orientation = orientation;
        if(orientation == OwnToOppMapOrientation.UP_DOWN || orientation == OwnToOppMapOrientation.DOWN_UP) {
            this.maxX = 9; 
            this.maxY = 9; 
        } else {
            this.maxX = 19;
            this.maxY = 4;
        }
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
        this.gameMapNodes = nodes;
        this.orientation = orientation;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    /**
     * Default constructor for GameMap.
     */
    public GameMap() {
        this.gameMapNodes = new ArrayList<>();
        this.orientation = null;
        this.maxX = 0;
        this.maxY = 0;
    }

    /**
     * Gets the player's own half map.
     * @return The player's own half map.
     */
    public PlayerHalfMap getOwnHalfMap() {
        PlayerHalfMap ownHalfMap = new PlayerHalfMap();
        if(this.gameMapNodes.size() == 50){
            for(MapNode node : gameMapNodes) {
                ownHalfMap.addMapNode(node);
            }
        }
        else{
            for (MapNode node : gameMapNodes) {
                switch (orientation) {
                    case UP_DOWN:
                        if (node.getY() <= 4) {
                            ownHalfMap.addMapNode(node);
                        }
                        break;
                    case DOWN_UP:
                        if (node.getY() >= 5) {
                            ownHalfMap.addMapNode(node);
                        }
                        break;
                    case LEFT_RIGHT:
                        if (node.getX() <= 9) {
                            ownHalfMap.addMapNode(node);
                        }
                        break;
                    case RIGHT_LEFT:
                        if (node.getX() >= 10) {
                            ownHalfMap.addMapNode(node);
                        }
                        break;
                }
            }
        }
        return ownHalfMap;
            
    }

    /**
     * Gets the opponent's half map.
     * @return The opponent's half map.
     */
    public PlayerHalfMap getOpponentHalfMap() {
        PlayerHalfMap opponentHalfMap = new PlayerHalfMap();
        for (MapNode node : gameMapNodes) {
            switch (orientation) {
                case UP_DOWN:
                    if (node.getY() >= 5) {
                        opponentHalfMap.addMapNode(node);
                    }
                    break;
                case DOWN_UP:
                    if (node.getY() <= 4) {
                        opponentHalfMap.addMapNode(node);
                    }
                    break;
                case LEFT_RIGHT:
                    if (node.getX() >= 10) {
                        opponentHalfMap.addMapNode(node);
                    }
                    break;
                case RIGHT_LEFT:
                    if (node.getX() <= 9) {
                        opponentHalfMap.addMapNode(node);
                    }
                    break;
            }
        }
        return opponentHalfMap;
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
        return maxX;
    }

    /**
     * Sets the maximum X coordinate.
     * @param maxX The maximum X coordinate.
     */
    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    /**
     * Gets the maximum Y coordinate.
     * @return The maximum Y coordinate.
     */
    public int getMaxY() {
        return maxY;
    }

    /**
     * Sets the maximum Y coordinate.
     * @param maxY The maximum Y coordinate.
     */
    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    /**
     * Gets the total number of nodes in the map.
     * @return The total number of nodes in the map.
     */
    public int getContentSize() {
        return gameMapNodes.size();
    }

    /**
     * Gets the MapNode at the specified coordinates.
     * @param x X-coordinate.
     * @param y Y-coordinate.
     * @return The MapNode at the specified coordinates.
     * @throws IllegalArgumentException if coordinates are invalid.
     */
    public MapNode getNode(int x, int y) {
        for (MapNode node : gameMapNodes) {
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
        return ownHalfMap.getFortNode();
    }

    /**
     * gets the map nodes of the game map.
     * @return An ArrayList of MapNode objects representing the game map nodes.
     */
    public ArrayList<MapNode> getGameMapNodes() {
        return gameMapNodes;
    }
    /**
     * Checks if the given node is in the player's own half of the map.
     * The method uses the map's orientation to determine the player's half.
     * @param node The MapNode to check.
     * @return True if the node is in the player's own half, false otherwise.
     */
    public boolean isNodeInOwnHalf(MapNode node) {
        if (node == null) {
            throw new IllegalArgumentException("gameMap Half finder :Node cannot be null");
        }
        switch (orientation) {
            case UP_DOWN:
                return node.getY() <= 4;
            case DOWN_UP:
                return node.getY() >= 5;
            case LEFT_RIGHT:
                return node.getX() <= 9;
            case RIGHT_LEFT:
                return node.getX() >= 10;
            default:
                throw new IllegalArgumentException("Invalid orientation: " + orientation);
        }
    }
    /**
     * Returns a string representation of the GameMap.
     * @return A string representation of the GameMap.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GameMap{orientation=").append(orientation)
          .append(", maxX=").append(maxX)
          .append(", maxY=").append(maxY)
          .append(", nodes=[");
        for (MapNode node : gameMapNodes) {
            sb.append(node.toString()).append(", ");
        }
        sb.append("]}");
        return sb.toString();
    }

}
