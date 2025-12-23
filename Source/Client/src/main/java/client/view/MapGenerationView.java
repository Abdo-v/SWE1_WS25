package client.view;

import client.model.mapper.MapNode;
import client.model.mapper.PlayerHalfMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
        Objects.requireNonNull(title, "title is required");
        Objects.requireNonNull(halfMap, "halfMap is required");

        if (halfMap.getMapNodes().isEmpty()) {
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
                Optional<MapNode> node = Optional.ofNullable(nodeMap.get(x + "," + y));
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

    private static String renderNode(Optional<MapNode> node) {
        if (node.filter(MapNode::isFortPresent).isPresent()) {
            return CLIIcons.OWN_FORT;
        }

        return node
                .map(MapNode::getTerrain)
                .map(terrain -> {
                    switch (terrain) {
                        case GRASS:
                            return CLIIcons.GRASS;
                        case MOUNTAIN:
                            return CLIIcons.MOUNTAIN;
                        case WATER:
                            return CLIIcons.WATER;
                        default:
                            return CLIIcons.UNKNOWN;
                    }
                })
                .orElse(CLIIcons.UNKNOWN);
    }
}
