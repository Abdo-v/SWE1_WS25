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
 *   <li>{@link MapGenerationView} for initial half-map visualization after generation</li>
 *   <li>{@link MapValidationInternalsView} for technical validation internals (System.err)</li>
 * </ul>
 */
public class CLIHandler implements client.observer.util.Observer {

    private Optional<GameState> gameState = Optional.empty();
    private static boolean reduced;
    private final MapSnapshotView snapshotView = new MapSnapshotView();
    private final MapGenerationView mapGenerationView = new MapGenerationView();

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

    private void visualizeMap(MapVisualizationType mapType) {
        MapVisualizationType safeType = Objects.requireNonNullElse(mapType, MapVisualizationType.UNKNOWN);

        if (this.gameState.isEmpty()) {
            switch (safeType) {
                case OWN:
                    System.out.println("Own map visual: map not available");
                    break;
                case OPPONENT:
                    System.out.println("Opponent map visual: map not available or incomplete");
                    break;
                case FULL:
                    System.out.println("Full map visual: map not available or incomplete");
                    break;
                default:
                    System.out.println("Invalid map type. Use 'own', 'opponent', or 'full'.");
            }
            return;
        }

        snapshotView.visualize(this.gameState.orElseThrow(), safeType);
    }


}
