package client.view;

import client.model.GameState;
import client.model.mapper.MapNode;

import java.util.Objects;
import java.util.Optional;

/**
 * Shared rendering logic for map cells.
 *
 * <p>Both snapshot output and dynamic output should use the same priority rules:
 * clash > player > opponent > treasure > forts > terrain.
 */
public final class MapCellRenderer {

    private MapCellRenderer() {
    }

    public static String renderCell(
            GameState state,
            Optional<MapNode> node,
            Optional<MapNode> myPosition,
            Optional<MapNode> opponentPosition,
            boolean myHasTreasure,
            boolean opponentHasTreasure
    ) {
        Objects.requireNonNull(state, "state is required");
        Objects.requireNonNull(node, "node is required");

        if (node.isEmpty()) {
            return CLIIcons.UNKNOWN;
        }

        MapNode cell = node.orElseThrow();

        // Priority 1: Clash
        if (myPosition.filter(pos -> pos.equals(cell)).isPresent()
            && opponentPosition.filter(pos -> pos.equals(cell)).isPresent()) {
            return CLIIcons.CLASH;
        }

        // Priority 2: Player
        if (myPosition.filter(pos -> pos.equals(cell)).isPresent()) {
            return myHasTreasure ? CLIIcons.PLAYER_WITH_TREASURE : CLIIcons.PLAYER;
        }

        // Priority 3: Opponent
        if (opponentPosition.filter(pos -> pos.equals(cell)).isPresent()) {
            return opponentHasTreasure ? CLIIcons.OPPONENT_WITH_TREASURE : CLIIcons.OPPONENT;
        }

        // Priority 4: Treasure (only shown until own treasure collected)
        if (!myHasTreasure && state.getTreasurePosition().filter(tp -> tp.equals(cell)).isPresent()) {
            return CLIIcons.TREASURE;
        }

        // Priority 5: Forts
        if (cell.isFortPresent()) {
            if (state.getOwnFortPosition().filter(fp -> fp.equals(cell)).isPresent()) {
                return CLIIcons.OWN_FORT;
            }
            return CLIIcons.OPPONENT_FORT;
        }

        // Priority 6: Terrain
        switch (cell.getTerrain()) {
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
