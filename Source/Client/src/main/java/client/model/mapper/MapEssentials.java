package client.model.mapper;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Shared, minimal map component that stores common map state.
 *
 * <p>Intentionally contains no game rules (e.g. half-map size limits).
 */
public final class MapEssentials {

    private int maxX;
    private int maxY;
    private final ArrayList<MapNode> nodes;

    public MapEssentials() {
        this(new ArrayList<>(), 0, 0);
    }

    public MapEssentials(ArrayList<MapNode> initialNodes, int maxX, int maxY) {
        this.nodes = new ArrayList<>(Objects.requireNonNull(initialNodes, "initialNodes"));
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public boolean addNode(MapNode node) {
        Objects.requireNonNull(node, "node");

        this.maxX = Math.max(this.maxX, node.getX());
        this.maxY = Math.max(this.maxY, node.getY());
        return nodes.add(node);
    }

    public ArrayList<MapNode> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<MapNode> newNodes) {
        nodes.clear();
        nodes.addAll(Objects.requireNonNull(newNodes, "newNodes"));
        recomputeBoundsFromNodes();
    }

    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public int size() {
        return nodes.size();
    }

    private void recomputeBoundsFromNodes() {
        int computedMaxX = 0;
        int computedMaxY = 0;
        for (MapNode node : nodes) {
            if (node == null) {
                continue;
            }
            computedMaxX = Math.max(computedMaxX, node.getX());
            computedMaxY = Math.max(computedMaxY, node.getY());
        }
        this.maxX = computedMaxX;
        this.maxY = computedMaxY;
    }
}
