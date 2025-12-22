package client.view;

import client.model.GameState;
import client.model.PlayerState;
import client.model.StaticColors;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;

import java.io.PrintWriter;
import java.io.StringWriter;

public class DynamicCLIGameView implements client.observer.util.Observer {

    private GameState currentGameState;
    private boolean dynamicModeActive = false;
    private boolean firstRenderDone = false; // To manage clearing strategy
    private int linesRenderedInPreviousFrame = 0; // New field

    // Emojis for map elements
    private static final String GRASS_ICON = "🟩";
    private static final String MOUNTAIN_ICON = "⬜"; // grey square instead ⛰️
    private static final String WATER_ICON = "🟦";
    private static final String PLAYER_ICON = "🦸"; // hero icon instead 👤
    private static final String OPPONENT_ICON = "👹";
    private static final String OWN_FORT_ICON = "🏰";
    private static final String OPPONENT_FORT_ICON = "🏯";
    private static final String TREASURE_ICON = "💎";
    private static final String CLASH_ICON = "💥";
    private static final String UNKNOWN_ICON = "❓";

    /**
     * Enables dynamic rendering of the game view.
     * Subsequent calls to update will render the game state.
     */
    public void enableDynamicMode() {
        this.dynamicModeActive = true;
        this.firstRenderDone = false; // Reset when enabling
        this.linesRenderedInPreviousFrame = 0; // Reset line count
        if (this.currentGameState != null) {
            render();
            // Note: firstRenderDone will be set to true at the end of the render() call
        }
    }

    /**
     * Disables dynamic rendering of the game view.
     * Subsequent calls to update will not render the game state.
     */
    public void disableDynamicMode() {
        this.dynamicModeActive = false;
        // No need to reset firstRenderDone here, as it's reset on enable
    }
    
    @Override
    public void update(GameState gameState) {
        this.currentGameState = gameState;
        // Ensure dynamicModeActive is checked before rendering
        if (this.dynamicModeActive && this.currentGameState != null) {
            render();
        }
    }

    private void render() {
        // Clear based on the height of the *previous* frame
        if (firstRenderDone && linesRenderedInPreviousFrame > 0) {
            System.out.print("\033[" + linesRenderedInPreviousFrame + "A"); // Move cursor up
            System.out.print("\033[J"); // Clear from cursor to end of screen
        }

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        int playerInfoLines = displayPlayerInfo(printWriter); // Pass writer, get line count
        int mapLines = displayMap(printWriter);             // Pass writer, get line count
        printWriter.flush(); // Ensure all content is in stringWriter

        String currentFrameOutput = stringWriter.toString();
        int totalLinesThisFrame = playerInfoLines + mapLines;

        System.out.print(currentFrameOutput);
        System.out.flush(); // Ensure all output is written

        linesRenderedInPreviousFrame = totalLinesThisFrame;
        // Set firstRenderDone only if something was actually rendered to prevent issues with empty initial renders
        if (!firstRenderDone && totalLinesThisFrame > 0) {
            firstRenderDone = true;
        }
    }

    private int displayPlayerInfo(PrintWriter out) { // Takes PrintWriter, returns line count
        int lines = 0;
        if (currentGameState == null) {
            return lines;
        }

        var myState = currentGameState.getCurrentPlayerState();
        var opponentState = currentGameState.getEnemyPlayerState();

        out.println("--- Game State ---"); lines++;

        if (myState.isPresent()) {
            out.println(); lines++; // For the original behavior of println("\n" + ...)
            out.println(PLAYER_ICON + " Your Player (" + myState.get().getPlayerID() + "):" ); lines++;
            //out.println("  Status: " + (myState.getStatus() != null ? myState.getStatus().toString() : "N/A")); lines++;
            myState.get().getCurrentPosition()
                    .ifPresentOrElse(
                            pos -> {
                                out.println(StaticColors.CYAN  + "  Position: (" + pos.getX() + "," + pos.getY() + ")" + StaticColors.RESET);
                            },
                            () -> out.println("  Position: N/A" + StaticColors.RESET)
                    );
            lines++;
            out.println(StaticColors.YELLOW + StaticColors.BOLD + "  Treasure collected: " + StaticColors.RESET + (myState.get().hasCollectedTreasure() ? "Yes" : "No")); lines++;
        } else {
            out.println(); lines++;
            out.println(PLAYER_ICON + " Your Player: Data N/A"); lines++;
        }

        if (opponentState.isPresent()) {
            out.println(); lines++;
            out.println(OPPONENT_ICON + " Opponent (" + opponentState.get().getPlayerID() + "):" ); lines++;
            //out.println("  Status: " + (opponentState.getStatus() != null ? opponentState.getStatus().toString() : "N/A")); lines++;
            opponentState.get().getCurrentPosition()
                    .ifPresentOrElse(
                            pos -> {
                                out.println(StaticColors.PURPLE + "  Position: (" + pos.getX() + "," + pos.getY() + ")" + StaticColors.RESET);
                            },
                            () -> out.println("  Position: Unknown")
                    );
            lines++;
            out.println(StaticColors.YELLOW + StaticColors.BOLD + "  Treasure collected: " + StaticColors.RESET + (opponentState.get().hasCollectedTreasure() ? "Yes" : "No")); lines++;
        } else {
            out.println(); lines++;
            out.println(OPPONENT_ICON + " Opponent: Data N/A"); lines++;
        }
        out.println("--------------------"); lines++;
        return lines;
    }

    private int displayMap(PrintWriter out) { // Takes PrintWriter, returns line count
        int lines = 0;
        if (currentGameState == null) return lines;
        
        var mapOptional = currentGameState.getMap();
        if (mapOptional.isEmpty()) {
            out.println("Map data not available."); lines++;
            return lines;
        }

        GameMap map = mapOptional.orElseThrow();
        
        int mapHeight = map.getMaxY(); // Assuming getMaxY() is 0-indexed max row
        int mapWidth = map.getMaxX();   // Assuming getMaxX() is 0-indexed max col

        // The loop condition should be r <= mapHeight if mapHeight is the max index (e.g., 0 to 4 for 5 rows)
        // The loop condition should be c <= mapWidth if mapWidth is the max index
        if (mapHeight < 0 || mapWidth < 0) { // Adjusted check for 0-indexed max
             out.println("Map dimensions are invalid (max indices are negative)."); lines++;
             return lines;
        }

        var myPlayerState = currentGameState.getCurrentPlayerState();
        var opponentState = currentGameState.getEnemyPlayerState();

        for (int r = 0; r <= mapHeight; r++) {
            StringBuilder line = new StringBuilder("|");
            for (int c = 0; c <= mapWidth; c++) {
                String cellContent = getCellRepresentation(r, c, map, myPlayerState, opponentState, currentGameState);
                
                line.append(cellContent);
                
                if (c < mapWidth) { // Add space if not the last cell in the row (mapWidth is max index)
                    line.append(" ");
                }
            }
            line.append("|");
            out.println(line.toString()); lines++;
        }
        out.println("--------------------"); lines++;
        return lines;
    }

    private String getCellRepresentation(int r, int c, GameMap gameMap, java.util.Optional<PlayerState> myPlayerState, java.util.Optional<PlayerState> opponentState, GameState fullGameState) {
        MapNode currentCellNode = gameMap.getNode(c, r); // Get the MapNode object for comparison

        var myPosition = myPlayerState.flatMap(PlayerState::getCurrentPosition);
        var opponentPosition = opponentState.flatMap(PlayerState::getCurrentPosition);

        // Priority 1: Clash
        if (myPosition.filter(pos -> pos.equals(currentCellNode)).isPresent()
                && opponentPosition.filter(pos -> pos.equals(currentCellNode)).isPresent()) {
            return CLASH_ICON;
        }

        // Priority 2: Player
        if (myPosition.filter(pos -> pos.equals(currentCellNode)).isPresent()) {
            return PLAYER_ICON;
        }

        // Priority 3: Opponent
        if (opponentPosition.filter(pos -> pos.equals(currentCellNode)).isPresent()) {
            return OPPONENT_ICON;
        }
        
        MapNode node = currentCellNode; // We already fetched it

        // Priority 4: Treasure
        boolean treasureAlreadyCollected = myPlayerState
            .map(PlayerState::hasCollectedTreasure)
            .orElse(false);

        if (!treasureAlreadyCollected
            && fullGameState.getTreasurePosition().filter(tp -> tp.equals(node)).isPresent()) {
            return TREASURE_ICON;
        }

        // Priority 5: Forts
        if (node.isFortPresent()) {
            if (myPlayerState.isPresent() && fullGameState.getOwnFortPosition().filter(fp -> fp.equals(node)).isPresent()) {
                return OWN_FORT_ICON;
            } else {
                return OPPONENT_FORT_ICON; 
            }
        }
        
        // Priority 6: Terrain
        switch (node.getTerrain()) {
            case GRASS: return GRASS_ICON;
            case MOUNTAIN: return MOUNTAIN_ICON;
            case WATER: return WATER_ICON;
            default: return UNKNOWN_ICON;
        }
    }
}