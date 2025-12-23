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

    static CLIHandler createCLIHandler(GameMode gameMode) {
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

        // Modern MVC wiring (composition): the dynamic view subscribes to the specific model streams it needs.
        // The view coalesces quick successive updates so bulk changes don't render multiple times.
        dynamicView.update(gameState);
        gameState.mapChanges().subscribe(ignored -> dynamicView.requestRender());
        gameState.playerListChanges().subscribe(ignored -> dynamicView.requestRender());
        gameState.treasureCollectedChanges().subscribe(ignored -> dynamicView.requestRender());
        gameState.treasurePositionChanges().subscribe(ignored -> dynamicView.requestRender());
        gameState.opponentFortFoundChanges().subscribe(ignored -> dynamicView.requestRender());
        gameState.opponentFortPositionChanges().subscribe(ignored -> dynamicView.requestRender());

        wayFinder.setGameState(gameState);
        wayFinder.addSubObservers();
    }
}

