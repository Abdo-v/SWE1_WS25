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
        System.out.println(CLILegends.halfMapLegend());
        System.out.println(CLITexts.SEPARATOR_HALF_MAP);

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

        System.out.println(CLITexts.SEPARATOR_HALF_MAP);
    }

    private static String renderNode(MapNode node) {
        if (node == null) {
            return CLIIcons.UNKNOWN;
        }

        if (node.isFortPresent()) {
            return CLIIcons.OWN_FORT;
        }

        switch (node.getTerrain()) {
            case GRASS:
                return CLIIcons.GRASS;
            case MOUNTAIN:
                return CLIIcons.MOUNTAIN;
            case WATER:
                return CLIIcons.WATER;
            default:
                return CLIIcons.UNKNOWN;
        }
    }
}
