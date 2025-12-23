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

    public void visualize(GameState gameState, MapVisualizationType mapType) {
        Objects.requireNonNull(gameState, "gameState is required");
        Objects.requireNonNull(mapType, "mapType is required");

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
                    System.out.println("Full map visual: map incomplete");
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
        System.out.println(CLILegends.fullMapLegend());
        System.out.println(CLITexts.SEPARATOR_SHORT);

        var myState = state.getCurrentPlayerState();
        var opponentState = state.getEnemyPlayerState();
        var myPosition = myState.flatMap(PlayerState::getCurrentPosition);
        var opponentPosition = opponentState.flatMap(PlayerState::getCurrentPosition);

        final boolean myHasTreasure = myState.map(PlayerState::hasCollectedTreasure).orElse(false);
        final boolean opponentHasTreasure = opponentState.map(PlayerState::hasCollectedTreasure).orElse(false);

        for (int y = minY; y <= maxY; y++) {
            StringBuilder row = new StringBuilder("|");
            for (int x = minX; x <= maxX; x++) {
                Optional<MapNode> node = SafeMapNodeLookup.tryGetNode(map, x, y);
                row.append(MapCellRenderer.renderCell(state, node, myPosition, opponentPosition, myHasTreasure, opponentHasTreasure));

                if (x < maxX) {
                    row.append(' ');
                }
            }
            row.append('|');
            System.out.println(row);
        }

        System.out.println(CLITexts.SEPARATOR_SHORT);
    }
}
