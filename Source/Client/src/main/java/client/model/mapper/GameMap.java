package client.model.mapper;

import client.model.ModelTextConfig;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.List;

/**
 * Combined map container used by the client.
 *
 * <p>This class stores the full node list and exposes convenience views for "own" vs "opponent"
 * half-maps based on {@link OwnToOppMapOrientation}.
 */
public class GameMap {
    private final MapEssentials map;
    private final Optional<OwnToOppMapOrientation> orientation;

    private static final String ORIENTATION_REQUIRED_MESSAGE = ModelTextConfig.ERROR_ORIENTATION_REQUIRED;

    public GameMap(ArrayList<MapNode> nodes, OwnToOppMapOrientation orientation, int maxX, int maxY) {
        this.orientation = Optional.of(Objects.requireNonNull(orientation, ModelTextConfig.REQUIRE_ORIENTATION));
        this.map = new MapEssentials(Objects.requireNonNull(nodes, ModelTextConfig.REQUIRE_NODES), maxX, maxY);
    }

    public GameMap() {
        this.map = new MapEssentials(new ArrayList<>(), 0, 0);
        this.orientation = Optional.empty();
    }

    public PlayerHalfMap getOwnHalfMap() {
        if (map.size() == HalfMapDimensions.TOTAL_NODES) {
            return buildHalfMap(node -> true);
        }

        return buildHalfMap(ownHalfPredicate());
    }

    public PlayerHalfMap getOpponentHalfMap() {
        return buildHalfMap(ownHalfPredicate().negate());
    }

    public OwnToOppMapOrientation getOrientation() {
        return orientation.orElseThrow(() -> new IllegalStateException(ORIENTATION_REQUIRED_MESSAGE));
    }

    public int getMaxX() {
        return map.getMaxX();
    }

    public int getMaxY() {
        return map.getMaxY();
    }

    public int getContentSize() {
        return map.size();
    }

    /**
     * Looks up a node by coordinates.
     *
     * @throws IllegalArgumentException if the coordinates are outside the known map content.
     */
    public MapNode getNode(int x, int y) {
        for (MapNode node : map.getNodes()) {
            if (node.getX() == x && node.getY() == y) {
                return node;
            }
        }
        throw new IllegalArgumentException(ModelTextConfig.invalidCoordinatesMessage(x, y));
    }

    public List<MapNode> getGameMapNodes() {
        return map.getNodes();
    }

    /** True if the node belongs to the local player's half given the current orientation. */
    public boolean isNodeInOwnHalf(MapNode node) {
        Objects.requireNonNull(node, ModelTextConfig.REQUIRE_NODE);
        return ownHalfPredicate().test(node);
    }

    private PlayerHalfMap buildHalfMap(Predicate<MapNode> includeNode) {
        Objects.requireNonNull(includeNode, ModelTextConfig.REQUIRE_INCLUDE_NODE);

        PlayerHalfMap halfMap = new PlayerHalfMap();
        for (MapNode node : map.getNodes()) {
            if (includeNode.test(node)) {
                halfMap.addMapNode(node);
            }
        }
        return halfMap;
    }

    private Predicate<MapNode> ownHalfPredicate() {
        if (orientation.isEmpty()) {
            throw new IllegalStateException(ORIENTATION_REQUIRED_MESSAGE);
        }

        return switch (orientation.get()) {
            case UP_DOWN -> node -> node.getY() <= HalfMapDimensions.HEIGHT - 1;
            case DOWN_UP -> node -> node.getY() >= HalfMapDimensions.HEIGHT;
            case LEFT_RIGHT -> node -> node.getX() <= HalfMapDimensions.WIDTH - 1;
            case RIGHT_LEFT -> node -> node.getX() >= HalfMapDimensions.WIDTH;
        };
    }
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
