package client.controller;

import client.model.GameMode;
import client.model.GameState;
import client.model.ai.WayFinder;
import client.view.CLIHandler;
import client.view.DynamicCLIGameView;

import java.util.Objects;

/**
 * Centralizes the wiring of observers and AI components for a {@link GameState}.
 *
 * Kept separate from {@link GameManager} to reduce responsibilities and keep construction consistent.
 */
final class GameManagerWiring {

    private GameManagerWiring() {
    }

    static CLIHandler createCliHandler(GameMode gameMode) {
        return new CLIHandler(Objects.requireNonNullElse(gameMode, GameMode.UNKNOWN));
    }

    static void wireObservers(GameState gameState, CLIHandler cliHandler, WayFinder wayFinder, DynamicCLIGameView dynamicView) {
        Objects.requireNonNull(gameState, "gameState is required");
        Objects.requireNonNull(cliHandler, "cliHandler is required");
        Objects.requireNonNull(wayFinder, "wayFinder is required");
        Objects.requireNonNull(dynamicView, "dynamicView is required");

        // Modern MVC wiring (composition): CLI only needs map updates.
        // Keep the old behavior of setting the initial state once.
        cliHandler.update(gameState);
        gameState.mapChanges().subscribe(ignored -> cliHandler.update(gameState));

        gameState.addObserver(wayFinder);
        gameState.addObserver(dynamicView);

        wayFinder.setGameState(gameState);
        wayFinder.addSubObservers();
    }
}

