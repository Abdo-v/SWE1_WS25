package client.view;

import client.model.GameState;
import client.model.PlayerState;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;

import java.util.Objects;
import java.util.Optional;

/**
 * Emoji-based, one-time map visualization.
 *
 * <p>Use this for snapshot-style output (e.g., initial FULL map print) to keep the visual
 * language consistent with {@link DynamicCLIGameView}.
 */
public class MapSnapshotView {

    // Terrain
    private static final String GRASS_ICON = "🟩";
    private static final String MOUNTAIN_ICON = "⬜";
    private static final String WATER_ICON = "🟦";

    // Entities
    private static final String PLAYER_ICON = "🫤";
    private static final String PLAYER_WITH_TREASURE_ICON = "🤑";
    private static final String OPPONENT_ICON = "👿";
    private static final String OPPONENT_WITH_TREASURE_ICON = "👹";
    private static final String OWN_FORT_ICON = "🏰";
    private static final String OPPONENT_FORT_ICON = "🏯";
    private static final String TREASURE_ICON = "💎";
    private static final String CLASH_ICON = "💥";
    private static final String UNKNOWN_ICON = "❓";

    public void visualize(GameState gameState, MapVisualizationType mapType) {
        Objects.requireNonNull(gameState, "gameState must not be null");
        Objects.requireNonNull(mapType, "mapType must not be null");

        Optional<GameMap> mapOpt = gameState.getMap();
        if (mapOpt.isEmpty() || mapOpt.get().getContentSize() == 0) {
            System.out.println(mapType.name() + " map visual: map not available");
            return;
        }

        GameMap map = mapOpt.get();

        switch (mapType) {
            case OWN:
                printMap(gameState, map, 0, 0, map.getMaxX(), map.getMaxY(), "Own Map Snapshot");
                break;
            case OPPONENT:
                if (map.getContentSize() != 100) {
                    System.out.println("Opponent map visual: map not available or incomplete");
                    return;
                }
                printMap(gameState, map, 0, 0, map.getMaxX(), map.getMaxY(), "Opponent Map Snapshot");
                break;
            case FULL:
                if (map.getContentSize() != 100) {
                    System.out.println("Full map visual: map not available or incomplete");
                    return;
                }
                printMap(gameState, map, 0, 0, map.getMaxX(), map.getMaxY(), "Full Map Snapshot");
                break;
            default:
                System.out.println("Invalid map type. Use 'own', 'opponent', or 'full'.");
        }
    }

    private void printMap(GameState state, GameMap map, int minX, int minY, int maxX, int maxY, String title) {
        System.out.println("\n" + title + " (" + (maxX + 1) + "x" + (maxY + 1) + ") - Orientation: " + map.getOrientation().getName());
        System.out.println("Legend: "
                + GRASS_ICON + "=grass "
                + MOUNTAIN_ICON + "=mountain "
                + WATER_ICON + "=water "
                + OWN_FORT_ICON + "/" + OPPONENT_FORT_ICON + "=fort "
                + TREASURE_ICON + "=treasure "
                + PLAYER_ICON + "/" + PLAYER_WITH_TREASURE_ICON + "=you "
                + OPPONENT_ICON + "/" + OPPONENT_WITH_TREASURE_ICON + "=opponent "
                + CLASH_ICON + "=clash");
        System.out.println("--------------------");

        var myState = state.getCurrentPlayerState();
        var opponentState = state.getEnemyPlayerState();
        var myPosition = myState.flatMap(PlayerState::getCurrentPosition);
        var opponentPosition = opponentState.flatMap(PlayerState::getCurrentPosition);

        final boolean myHasTreasure = myState.map(PlayerState::hasCollectedTreasure).orElse(false);
        final boolean opponentHasTreasure = opponentState.map(PlayerState::hasCollectedTreasure).orElse(false);

        for (int y = minY; y <= maxY; y++) {
            StringBuilder row = new StringBuilder("|");
            for (int x = minX; x <= maxX; x++) {
                MapNode node;
                try {
                    node = map.getNode(x, y);
                } catch (Exception e) {
                    node = null;
                }

                row.append(renderCell(state, node, myPosition, opponentPosition, myHasTreasure, opponentHasTreasure));

                if (x < maxX) {
                    row.append(' ');
                }
            }
            row.append('|');
            System.out.println(row);
        }

        System.out.println("--------------------");
    }

    private String renderCell(
            GameState state,
            MapNode node,
            Optional<MapNode> myPosition,
            Optional<MapNode> opponentPosition,
            boolean myHasTreasure,
            boolean opponentHasTreasure
    ) {
        if (node == null) {
            return UNKNOWN_ICON;
        }

        // Same priority order as DynamicCLIGameView
        if (myPosition.filter(pos -> pos.equals(node)).isPresent()
                && opponentPosition.filter(pos -> pos.equals(node)).isPresent()) {
            return CLASH_ICON;
        }

        if (myPosition.filter(pos -> pos.equals(node)).isPresent()) {
            return myHasTreasure ? PLAYER_WITH_TREASURE_ICON : PLAYER_ICON;
        }

        if (opponentPosition.filter(pos -> pos.equals(node)).isPresent()) {
            return opponentHasTreasure ? OPPONENT_WITH_TREASURE_ICON : OPPONENT_ICON;
        }

        if (!myHasTreasure && state.getTreasurePosition().filter(tp -> tp.equals(node)).isPresent()) {
            return TREASURE_ICON;
        }

        if (node.isFortPresent()) {
            if (state.getOwnFortPosition().filter(fp -> fp.equals(node)).isPresent()) {
                return OWN_FORT_ICON;
            }
            return OPPONENT_FORT_ICON;
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
