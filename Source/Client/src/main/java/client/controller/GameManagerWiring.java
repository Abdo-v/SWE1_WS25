package client.controller;

import client.model.GameMode;
import client.model.GameState;
import client.model.ai.WayFinder;
import client.view.CLIHandler;
import client.view.DynamicCLIGameView;

/**
 * Centralizes the wiring of observers and AI components for a {@link GameState}.
 *
 * Kept separate from {@link GameManager} to reduce responsibilities and keep construction consistent.
 */
final class GameManagerWiring {

    private GameManagerWiring() {
    }

    static CLIHandler createCliHandler(GameMode gameMode) {
        return new CLIHandler(gameMode != null ? gameMode : GameMode.UNKNOWN);
    }

    static void wireObservers(GameState gameState, CLIHandler cliHandler, WayFinder wayFinder, DynamicCLIGameView dynamicView) {
        if (gameState == null) {
            throw new IllegalArgumentException("gameState must not be null");
        }
        if (cliHandler == null) {
            throw new IllegalArgumentException("cliHandler must not be null");
        }
        if (wayFinder == null) {
            throw new IllegalArgumentException("wayFinder must not be null");
        }
        if (dynamicView == null) {
            throw new IllegalArgumentException("dynamicView must not be null");
        }

        gameState.addObserver(cliHandler);
        gameState.addObserver(wayFinder);
        gameState.addObserver(dynamicView);

        wayFinder.setGameState(gameState);
        wayFinder.addSubObservers();
    }
}
