package client.view;

import client.model.GameState;
import client.model.PlayerState;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;

import java.util.Objects;
import java.util.Optional;

/**
 * Produces a complete, ready-to-print CLI "frame" from a {@link GameState}.
 *
 * <p>The renderer returns plain text (including newlines) so the caller can control
 * output flushing and timing. Cell-level rendering is delegated to {@link MapCellRenderer}
 * to keep snapshot and dynamic output consistent.
 */
final class DynamicCLIFrameRenderer {

    private static final String MAP_FOOTER = "--------------------";

    RenderedCLIFrame render(GameState gameState) {
        Objects.requireNonNull(gameState, "gameState is required");

        RenderedCLISection playerInfo = renderPlayerInfo(gameState);
        RenderedCLISection map = renderMap(gameState);

        String output = playerInfo.text + map.text;
        return new RenderedCLIFrame(output, playerInfo.lineCount + map.lineCount);
    }

    private RenderedCLISection renderPlayerInfo(GameState gameState) {
        StringBuilder sb = new StringBuilder();
        int lines = 0;

        var myState = gameState.getCurrentPlayerState();
        var opponentState = gameState.getEnemyPlayerState();

        sb.append(CLITexts.GAME_STATE_HEADER).append('\n');
        lines++;

        if (myState.isPresent()) {
            boolean myHasTreasure = myState.get().hasCollectedTreasure();
            sb.append('\n');
            lines++;

            sb.append((myHasTreasure ? CLIIcons.PLAYER_WITH_TREASURE : CLIIcons.PLAYER))
                    .append(" Your Player (")
                    .append(myState.get().getPlayerID())
                    .append("):")
                    .append('\n');
            lines++;

            Optional<MapNode> myPos = myState.get().getCurrentPosition();
            if (myPos.isPresent()) {
                sb.append("  Position: (")
                        .append(myPos.get().getX())
                        .append(',')
                        .append(myPos.get().getY())
                        .append(")")
                        .append('\n');
            } else {
                sb.append("  Position: N/A\n");
            }
            lines++;

            sb.append("  Treasure collected: ").append(myHasTreasure ? "Yes" : "No").append('\n');
            lines++;
        } else {
            sb.append('\n');
            lines++;
            sb.append(CLIIcons.PLAYER).append(" Your Player: Data N/A\n");
            lines++;
        }

        if (opponentState.isPresent()) {
            boolean opponentHasTreasure = opponentState.get().hasCollectedTreasure();
            sb.append('\n');
            lines++;

            sb.append((opponentHasTreasure ? CLIIcons.OPPONENT_WITH_TREASURE : CLIIcons.OPPONENT))
                    .append(" Opponent (")
                    .append(opponentState.get().getPlayerID())
                    .append("):")
                    .append('\n');
            lines++;

            Optional<MapNode> oppPos = opponentState.get().getCurrentPosition();
            if (oppPos.isPresent()) {
                sb.append("  Position: (")
                        .append(oppPos.get().getX())
                        .append(',')
                        .append(oppPos.get().getY())
                        .append(")")
                        .append('\n');
            } else {
                sb.append("  Position: Unknown\n");
            }
            lines++;

            sb.append("  Treasure collected: ").append(opponentHasTreasure ? "Yes" : "No").append('\n');
            lines++;
        } else {
            sb.append('\n');
            lines++;
            sb.append(CLIIcons.OPPONENT).append(" Opponent: Data N/A\n");
            lines++;
        }

        sb.append(CLITexts.GAME_STATE_FOOTER).append('\n');
        lines++;

        return new RenderedCLISection(sb.toString(), lines);
    }

    private RenderedCLISection renderMap(GameState gameState) {
        var mapOptional = gameState.getMap();
        if (mapOptional.isEmpty()) {
            return new RenderedCLISection(CLITexts.MAP_NOT_AVAILABLE + "\n", 1);
        }

        GameMap map = mapOptional.orElseThrow();
        int mapHeight = map.getMaxY();
        int mapWidth = map.getMaxX();

        if (mapHeight < 0 || mapWidth < 0) {
            return new RenderedCLISection(CLITexts.MAP_DIMENSIONS_INVALID + "\n", 1);
        }

        var myPlayerState = gameState.getCurrentPlayerState();
        var opponentState = gameState.getEnemyPlayerState();

        StringBuilder sb = new StringBuilder();
        int lines = 0;

        for (int r = 0; r <= mapHeight; r++) {
            StringBuilder line = new StringBuilder("|");
            for (int c = 0; c <= mapWidth; c++) {
                String cell = renderCell(r, c, map, myPlayerState, opponentState, gameState);
                line.append(cell);
                if (c < mapWidth) {
                    line.append(' ');
                }
            }
            line.append('|');
            sb.append(line).append('\n');
            lines++;
        }

        sb.append(MAP_FOOTER).append('\n');
        lines++;

        return new RenderedCLISection(sb.toString(), lines);
    }

    private static String renderCell(int r, int c, GameMap gameMap, Optional<PlayerState> myPlayerState, Optional<PlayerState> opponentState, GameState fullGameState) {
        Optional<MapNode> currentCellNode = SafeMapNodeLookup.tryGetNode(gameMap, c, r);

        var myPosition = myPlayerState.flatMap(PlayerState::getCurrentPosition);
        var opponentPosition = opponentState.flatMap(PlayerState::getCurrentPosition);

        boolean myHasTreasure = myPlayerState.map(PlayerState::hasCollectedTreasure).orElse(false);
        boolean opponentHasTreasure = opponentState.map(PlayerState::hasCollectedTreasure).orElse(false);

        return MapCellRenderer.renderCell(fullGameState, currentCellNode, myPosition, opponentPosition, myHasTreasure, opponentHasTreasure);
    }

    record RenderedCLIFrame(String text, int lineCount) {
    }

    record RenderedCLISection(String text, int lineCount) {
    }
}
