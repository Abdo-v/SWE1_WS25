package client.controller;

import client.model.GameState;
import client.view.DynamicCLIGameView;
import client.view.MapVisualizationType;
import client.view.CLIHandler;
import client.view.MapSnapshotView;

/**
 * Owns map visualization decisions (snapshot vs dynamic) for the current {@link GameState}.
 */
final class GameVisualizationService {

    private final DynamicCLIGameView dynamicView;
    private final GameState gameState;
    private final MapSnapshotView snapshotView;

    GameVisualizationService(CLIHandler cliHandler, DynamicCLIGameView dynamicView, GameState gameState) {
        this.dynamicView = dynamicView;
        this.gameState = gameState;
        this.snapshotView = new MapSnapshotView();
    }

    /**
     * Renders a one-off map snapshot for the given CLI argument.
     *
     * <p>No-op in reduced mode.
     */
    void visualizeMap(String mapType) {
        visualizeMap(MapVisualizationType.fromCLIValue(mapType));
    }

    private void visualizeMap(MapVisualizationType mapType) {
        if (CLIHandler.isGameModeReduced()) {
            return;
        }
        snapshotView.visualize(gameState, mapType);
    }

    void enableDynamicVisualization() {
        dynamicView.enableDynamicMode();
    }

    void disableDynamicVisualization() {
        dynamicView.disableDynamicMode();
    }
}
