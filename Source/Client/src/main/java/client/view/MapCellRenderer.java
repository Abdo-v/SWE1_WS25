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
            MapNode node,
            Optional<MapNode> myPosition,
            Optional<MapNode> opponentPosition,
            boolean myHasTreasure,
            boolean opponentHasTreasure
    ) {
        Objects.requireNonNull(state, "state must not be null");

        if (node == null) {
            return CLIIcons.UNKNOWN;
        }

        // Priority 1: Clash
        if (myPosition.filter(pos -> pos.equals(node)).isPresent()
                && opponentPosition.filter(pos -> pos.equals(node)).isPresent()) {
            return CLIIcons.CLASH;
        }

        // Priority 2: Player
        if (myPosition.filter(pos -> pos.equals(node)).isPresent()) {
            return myHasTreasure ? CLIIcons.PLAYER_WITH_TREASURE : CLIIcons.PLAYER;
        }

        // Priority 3: Opponent
        if (opponentPosition.filter(pos -> pos.equals(node)).isPresent()) {
            return opponentHasTreasure ? CLIIcons.OPPONENT_WITH_TREASURE : CLIIcons.OPPONENT;
        }

        // Priority 4: Treasure (only shown until own treasure collected)
        if (!myHasTreasure && state.getTreasurePosition().filter(tp -> tp.equals(node)).isPresent()) {
            return CLIIcons.TREASURE;
        }

        // Priority 5: Forts
        if (node.isFortPresent()) {
            if (state.getOwnFortPosition().filter(fp -> fp.equals(node)).isPresent()) {
                return CLIIcons.OWN_FORT;
            }
            return CLIIcons.OPPONENT_FORT;
        }

        // Priority 6: Terrain
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
