package client.view;

import client.model.GameState;
import client.model.PlayerState;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class DynamicCLIGameView implements client.observer.util.Observer {

    private static final long RENDER_DEBOUNCE_MILLIS = 15L;
    private static final ScheduledExecutorService RENDER_EXECUTOR = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread t = new Thread(runnable, "DynamicCLIGameView-render");
            t.setDaemon(true);
            return t;
        }
    });

    private GameState currentGameState;
    private final Object renderLock = new Object();
    private ScheduledFuture<?> pendingRender;
    private boolean dynamicModeActive = false;

    /**
     * Enables dynamic rendering of the game view.
     * Subsequent calls to update will render the game state.
     */
    public void enableDynamicMode() {
        this.dynamicModeActive = true;
        if (this.currentGameState != null) {
            render();
        }
    }

    /**
     * Disables dynamic rendering of the game view.
     * Subsequent calls to update will not render the game state.
     */
    public void disableDynamicMode() {
        this.dynamicModeActive = false;
    }
    
    @Override
    public void update(GameState gameState) {
        this.currentGameState = Objects.requireNonNull(gameState, "gameState must not be null");
        requestRender();
    }

    /**
     * Schedules a render shortly in the future.
     * If multiple updates arrive quickly (e.g., during bulk model updates), renders are coalesced.
     */
    public void requestRender() {
        if (!this.dynamicModeActive || this.currentGameState == null) {
            return;
        }

        synchronized (renderLock) {
            if (pendingRender != null) {
                pendingRender.cancel(false);
            }
            pendingRender = RENDER_EXECUTOR.schedule(this::renderIfActive, RENDER_DEBOUNCE_MILLIS, TimeUnit.MILLISECONDS);
        }
    }

    private void renderIfActive() {
        if (this.dynamicModeActive && this.currentGameState != null) {
            render();
        }
    }

    private void render() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        int playerInfoLines = displayPlayerInfo(printWriter); // Pass writer, get line count
        int mapLines = displayMap(printWriter);             // Pass writer, get line count
        printWriter.flush(); // Ensure all content is in stringWriter

        String currentFrameOutput = stringWriter.toString();
        int totalLinesThisFrame = playerInfoLines + mapLines;
        if (totalLinesThisFrame <= 0) {
            return;
        }
        System.out.print(currentFrameOutput);
        System.out.flush(); // Ensure all output is written
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
            final boolean myHasTreasure = myState.get().hasCollectedTreasure();
            out.println(); lines++; // For the original behavior of println("\n" + ...)
            out.println((myHasTreasure ? CLIIcons.PLAYER_WITH_TREASURE : CLIIcons.PLAYER) + " Your Player (" + myState.get().getPlayerID() + "):" ); lines++;
            //out.println("  Status: " + (myState.getStatus() != null ? myState.getStatus().toString() : "N/A")); lines++;
            myState.get().getCurrentPosition()
                    .ifPresentOrElse(
                            pos -> {
                                out.println("  Position: (" + pos.getX() + "," + pos.getY() + ")");
                            },
                            () -> out.println("  Position: N/A")
                    );
            lines++;
            out.println("  Treasure collected: " + (myHasTreasure ? "Yes" : "No")); lines++;
        } else {
            out.println(); lines++;
            out.println(CLIIcons.PLAYER + " Your Player: Data N/A"); lines++;
        }

        if (opponentState.isPresent()) {
            final boolean opponentHasTreasure = opponentState.get().hasCollectedTreasure();
            out.println(); lines++;
            out.println((opponentHasTreasure ? CLIIcons.OPPONENT_WITH_TREASURE : CLIIcons.OPPONENT) + " Opponent (" + opponentState.get().getPlayerID() + "):" ); lines++;
            //out.println("  Status: " + (opponentState.getStatus() != null ? opponentState.getStatus().toString() : "N/A")); lines++;
            opponentState.get().getCurrentPosition()
                    .ifPresentOrElse(
                            pos -> {
                                out.println("  Position: (" + pos.getX() + "," + pos.getY() + ")");
                            },
                            () -> out.println("  Position: Unknown")
                    );
            lines++;
            out.println("  Treasure collected: " + (opponentHasTreasure ? "Yes" : "No")); lines++;
        } else {
            out.println(); lines++;
            out.println(CLIIcons.OPPONENT + " Opponent: Data N/A"); lines++;
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

        final boolean myHasTreasure = myPlayerState.map(PlayerState::hasCollectedTreasure).orElse(false);
        final boolean opponentHasTreasure = opponentState.map(PlayerState::hasCollectedTreasure).orElse(false);

        // Priority 1: Clash
        if (myPosition.filter(pos -> pos.equals(currentCellNode)).isPresent()
                && opponentPosition.filter(pos -> pos.equals(currentCellNode)).isPresent()) {
            return CLIIcons.CLASH;
        }

        // Priority 2: Player
        if (myPosition.filter(pos -> pos.equals(currentCellNode)).isPresent()) {
            return myHasTreasure ? CLIIcons.PLAYER_WITH_TREASURE : CLIIcons.PLAYER;
        }

        // Priority 3: Opponent
        if (opponentPosition.filter(pos -> pos.equals(currentCellNode)).isPresent()) {
            return opponentHasTreasure ? CLIIcons.OPPONENT_WITH_TREASURE : CLIIcons.OPPONENT;
        }
        
        MapNode node = currentCellNode; // We already fetched it

        // Priority 4: Treasure
        final boolean treasureAlreadyCollected = myHasTreasure;

        if (!treasureAlreadyCollected
            && fullGameState.getTreasurePosition().filter(tp -> tp.equals(node)).isPresent()) {
            return CLIIcons.TREASURE;
        }

        // Priority 5: Forts
        if (node.isFortPresent()) {
            if (myPlayerState.isPresent() && fullGameState.getOwnFortPosition().filter(fp -> fp.equals(node)).isPresent()) {
                return CLIIcons.OWN_FORT;
            } else {
                return CLIIcons.OPPONENT_FORT;
            }
        }
        
        // Priority 6: Terrain
        switch (node.getTerrain()) {
            case GRASS: return CLIIcons.GRASS;
            case MOUNTAIN: return CLIIcons.MOUNTAIN;
            case WATER: return CLIIcons.WATER;
            default: return CLIIcons.UNKNOWN;
        }
    }
}