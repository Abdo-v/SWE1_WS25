package client.controller;

import client.view.DynamicCLIGameView;
import client.view.MapVisualizationType;
import client.view.CLIHandler;

final class GameVisualizationService {

    private final CLIHandler cliHandler;
    private final DynamicCLIGameView dynamicView;

    GameVisualizationService(CLIHandler cliHandler, DynamicCLIGameView dynamicView) {
        this.cliHandler = cliHandler;
        this.dynamicView = dynamicView;
    }

    void visualizeMap(String mapType) {
        cliHandler.visualizeMap(mapType);
    }

    void visualizeMap(MapVisualizationType mapType) {
        cliHandler.visualizeMap(mapType);
    }

    void enableDynamicVisualization() {
        dynamicView.enableDynamicMode();
    }

    void disableDynamicVisualization() {
        dynamicView.disableDynamicMode();
    }
}
