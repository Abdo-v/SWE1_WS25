package client.view;

import client.model.mapper.MapNode;
import client.model.mapper.PlayerHalfMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * CLI visualization for the map generation phase.
 *
 * <p>Purpose: Provide a consistent, emoji-based visualization of the generated half map
 * directly after it is created (before sending to the server). This keeps the UI consistent
 * with the in-game emoji map visualization.
 */
public class MapGenerationView {

    // Terrain
    private static final String GRASS_ICON = "🟩";
    private static final String MOUNTAIN_ICON = "⬜";
    private static final String WATER_ICON = "🟦";

    // Entities (half-map only reliably contains own fort)
    private static final String OWN_FORT_ICON = "🏰";
    private static final String UNKNOWN_ICON = "❓";

    /**
     * Prints an emoji-based half map representation to {@link System#out}.
     *
     * @param halfMap half map to print
     * @param title title shown above the map
     */
    public void printHalfMap(PlayerHalfMap halfMap, String title) {
        Objects.requireNonNull(title, "title must not be null");

        if (halfMap == null || halfMap.getMapNodes().isEmpty()) {
            System.out.println("\n" + title + ": (no half-map data)");
            return;
        }

        System.out.println("\n" + title + " (Half Map):");
        System.out.println("Legend: " + GRASS_ICON + "=grass " + MOUNTAIN_ICON + "=mountain " + WATER_ICON + "=water " + OWN_FORT_ICON + "=fort");
        System.out.println("-------------------------");

        Map<String, MapNode> nodeMap = new HashMap<>();
        for (MapNode node : halfMap.getMapNodes()) {
            nodeMap.put(node.getX() + "," + node.getY(), node);
        }

        int maxX = halfMap.getMapNodes().stream().mapToInt(MapNode::getX).max().orElse(0);
        int maxY = halfMap.getMapNodes().stream().mapToInt(MapNode::getY).max().orElse(0);

        for (int y = 0; y <= maxY; y++) {
            StringBuilder row = new StringBuilder("|");
            for (int x = 0; x <= maxX; x++) {
                MapNode node = nodeMap.get(x + "," + y);
                row.append(renderNode(node));
                if (x < maxX) {
                    row.append(' ');
                }
            }
            row.append('|');
            System.out.println(row);
        }

        System.out.println("-------------------------");
    }

    private static String renderNode(MapNode node) {
        if (node == null) {
            return UNKNOWN_ICON;
        }

        if (node.isFortPresent()) {
            return OWN_FORT_ICON;
        }

        switch (node.getTerrain()) {
            case GRASS:
                return GRASS_ICON;
            case MOUNTAIN:
                return MOUNTAIN_ICON;
            case WATER:
                return WATER_ICON;
            default:
                return UNKNOWN_ICON;
        }
    }
}
