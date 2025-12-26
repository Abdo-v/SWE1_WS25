package client.view;

import client.model.GameState;
import client.model.GameMode;

import java.util.Objects;
import java.util.Optional;
/**
 * Legacy CLI handler.
 *
 * <p>This class is primarily used in wiring
 * (e.g., as a lightweight holder for the latest {@link client.model.GameState} and reduced-mode flag).
 *
 * <p>For assignment-compliant, emoji-based visualizations, prefer:
 * <ul>
 *   <li>{@link DynamicCLIGameView} for in-game visualization</li>
 *   <li>{@link MapValidationInternalsView} for technical validation internals (System.err)</li>
 * </ul>
 */
public class CLIHandler implements client.observer.util.Observer {

    private Optional<GameState> gameState = Optional.empty();
    private static boolean reduced;
    private final MapSnapshotView snapshotView = new MapSnapshotView();

    public CLIHandler(GameMode gameMode) {
        GameMode safeMode = Objects.requireNonNullElse(gameMode, GameMode.UNKNOWN);
        CLIHandler.reduced = safeMode.isReduced();
    }

    @Override
    public void update(GameState gameState ){
        this.gameState = Optional.of(Objects.requireNonNull(gameState, "gameState is required"));
        // to do : implement update on the output to the CLI
    }

    public static boolean isGameModeReduced() {
        return CLIHandler.reduced;
    }


}
