package client.model.mapper;

import client.model.ModelTextConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PlayerHalfMap {
    private final MapEssentials map;

    /**
     * Constructs a PlayerHalfMap with an empty list of map nodes.
     */
    public PlayerHalfMap() {
        this.map = new MapEssentials(new ArrayList<>(), 0, 0);
    }

    /**
     * Constructs a PlayerHalfMap with the given player ID.
     * @param playerID The ID of the player.
     */
    public PlayerHalfMap(String playerID) {
        Objects.requireNonNull(playerID, "playerID");
        this.map = new MapEssentials(new ArrayList<>(), 0, 0);

    }

    /**
     * Adds a map node to the half map.
     * @param mapNode The map node to add.
     * @return true if the node was added successfully, false otherwise.
     */
    public boolean addMapNode(MapNode mapNode) {
        Objects.requireNonNull(mapNode, "mapNode");
        if (map.size() >= HalfMapDimensions.TOTAL_NODES) {
            throw new IllegalStateException(ModelTextConfig.tooManyHalfMapNodesMessage(HalfMapDimensions.TOTAL_NODES));
        }
        return map.addNode(mapNode);
    }

    /**
     * Gets the map node that contains a fort in the half map.
     * @return An {@link Optional} containing the map node with a fort; empty if none exists.
     */
    public Optional<MapNode> getFortNode() {
        for (MapNode mapNode : map.getNodes()) {
            if (mapNode.isFortPresent()) {
                return Optional.of(mapNode);
            }
        }
        return Optional.empty();
    }

    /**
     * Gets a map node by its coordinates using == for parameters.
     * @return An {@link Optional} containing the map node at the specified coordinates; empty if not found.
     */
    public Optional<MapNode> getMapNode(int xIndex, int yIndex) {
        for (MapNode mapNode : map.getNodes()) {
            if (mapNode.getX() == xIndex && mapNode.getY() == yIndex) {
                return Optional.of(mapNode);
            }
        }
        return Optional.empty();
    }

    /**
     * Gets the list of map nodes.
     * @return An unmodifiable view of the map nodes.
     */
    public List<MapNode> getMapNodes() {
        return map.getNodes();
    }

}
