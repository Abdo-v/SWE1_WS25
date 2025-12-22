package client.model.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    public MapEssentials(List<MapNode> initialNodes, int maxX, int maxY) {
        Objects.requireNonNull(initialNodes, "initialNodes");
        for (MapNode node : initialNodes) {
            Objects.requireNonNull(node, "node");
        }
        this.nodes = new ArrayList<>(initialNodes);
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public boolean addNode(MapNode node) {
        Objects.requireNonNull(node, "node");

        this.maxX = Math.max(this.maxX, node.getX());
        this.maxY = Math.max(this.maxY, node.getY());
        return nodes.add(node);
    }

    /**
     * Returns an unmodifiable view of the current nodes.
     *
     * <p>This prevents callers from mutating internal state without updating bounds/invariants.
     * Use {@link #addNode(MapNode)} or {@link #setNodes(List)} to mutate.
     */
    public List<MapNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public void setNodes(List<MapNode> newNodes) {
        Objects.requireNonNull(newNodes, "newNodes");
        for (MapNode node : newNodes) {
            Objects.requireNonNull(node, "node");
        }
        nodes.clear();
        nodes.addAll(newNodes);
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
            computedMaxX = Math.max(computedMaxX, node.getX());
            computedMaxY = Math.max(computedMaxY, node.getY());
        }
        this.maxX = computedMaxX;
        this.maxY = computedMaxY;
    }
}
