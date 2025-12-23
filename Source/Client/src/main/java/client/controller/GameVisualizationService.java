package client.controller;

import client.model.GameState;
import client.view.DynamicCLIGameView;
import client.view.MapVisualizationType;
import client.view.CLIHandler;
import client.view.MapSnapshotView;

final class GameVisualizationService {

    private final CLIHandler cliHandler;
    private final DynamicCLIGameView dynamicView;
    private final GameState gameState;
    private final MapSnapshotView snapshotView;

    GameVisualizationService(CLIHandler cliHandler, DynamicCLIGameView dynamicView, GameState gameState) {
        this.cliHandler = cliHandler;
        this.dynamicView = dynamicView;
        this.gameState = gameState;
        this.snapshotView = new MapSnapshotView();
    }

    void visualizeMap(String mapType) {
        visualizeMap(MapVisualizationType.fromCLIValue(mapType));
    }

    void visualizeMap(MapVisualizationType mapType) {
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
