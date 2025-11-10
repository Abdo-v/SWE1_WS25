package client.model.mapper;

import java.util.ArrayList;

public class PlayerHalfMap {
    private ArrayList<MapNode> mapNodes;

    /**
     * Constructs a PlayerHalfMap with an empty list of map nodes.
     */
    public PlayerHalfMap() {
        this.mapNodes = new ArrayList<>();
    }

    /**
     * Constructs a PlayerHalfMap with the given player ID.
     * @param playerID The ID of the player.
     */
    public PlayerHalfMap(String playerID) {
        this.mapNodes = new ArrayList<>();

    }

    /**
     * Adds a map node to the half map.
     * @param mapNode The map node to add.
     * @return true if the node was added successfully, false otherwise.
     */
    public boolean addMapNode(MapNode mapNode) {
        if (mapNodes.size() < 50) {
            mapNodes.add(mapNode);
            return true;
        } else {
            throw new IllegalStateException("Cannot add more than 50 map nodes to a half map.");
        }
    }

    /**
     * Gets the map node that contains a fort in the half map.
     * @return The map node containing a fort, or null if none exists.
     */
    public MapNode getFortNode() {
        for (MapNode mapNode : mapNodes) {
            if (mapNode.isFortPresent()) {
                return mapNode;
            }
        }
        return null; // No fort found
    }

    /**
     * Gets a map node by its coordinates using == for parameters.
     * @param x_index The X coordinate of the node.
     * @param y_index The Y coordinate of the node.
     * @return The map node at the specified coordinates, or null if not found.
     */
    public MapNode getMapNode(int x_index, int y_index) {
        for (MapNode mapNode : mapNodes) {
            if (mapNode.getX() == x_index && mapNode.getY() == y_index) {
                return mapNode;
            }
        }
        return null;
    }

    /**
     * Gets the list of map nodes.
     * @return The list of map nodes.
     */
    public ArrayList<MapNode> getMapNodes() {
        return mapNodes;
    }
}
