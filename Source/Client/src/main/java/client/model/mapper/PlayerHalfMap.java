package client.model.mapper;

import client.model.ModelTextConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A single player's half-map.
 *
 * <p>This is the unit submitted to / validated against the game rules and is also used as a
 * convenient view when splitting a full map.
 */
public class PlayerHalfMap {
    private final MapEssentials map;
    public PlayerHalfMap() {
        this.map = new MapEssentials(new ArrayList<>(), 0, 0);
    }

    public PlayerHalfMap(String playerID) {
        Objects.requireNonNull(playerID, "playerID");
        this.map = new MapEssentials(new ArrayList<>(), 0, 0);

    }

    /** Adds a node, enforcing the half-map size limit. */
    public boolean addMapNode(MapNode mapNode) {
        Objects.requireNonNull(mapNode, "mapNode");
        if (map.size() >= HalfMapDimensions.TOTAL_NODES) {
            throw new IllegalStateException(ModelTextConfig.tooManyHalfMapNodesMessage(HalfMapDimensions.TOTAL_NODES));
        }
        return map.addNode(mapNode);
    }

    public Optional<MapNode> getFortNode() {
        for (MapNode mapNode : map.getNodes()) {
            if (mapNode.isFortPresent()) {
                return Optional.of(mapNode);
            }
        }
        return Optional.empty();
    }

    public Optional<MapNode> getMapNode(int xIndex, int yIndex) {
        for (MapNode mapNode : map.getNodes()) {
            if (mapNode.getX() == xIndex && mapNode.getY() == yIndex) {
                return Optional.of(mapNode);
            }
        }
        return Optional.empty();
    }

    public List<MapNode> getMapNodes() {
        return map.getNodes();
    }

}
