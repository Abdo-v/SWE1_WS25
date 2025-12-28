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
        return new CLIHandler(ControllerTextConfig.defaultIfMissing(gameMode, GameMode.UNKNOWN));
    }

    /**
     * Wires model observers/subscriptions for the CLI view, dynamic view, and AI.
     *
     * <p>Performs an initial view update, then subscribes to fine-grained change streams so
     * rendering can be coalesced without missing events.
     */
    static void wireObservers(GameState gameState, CLIHandler cliHandler, WayFinder wayFinder, DynamicCLIGameView dynamicView) {
        Objects.requireNonNull(gameState, ControllerTextConfig.REQUIRE_GAME_STATE);
        Objects.requireNonNull(cliHandler, ControllerTextConfig.REQUIRE_CLI_HANDLER);
        Objects.requireNonNull(wayFinder, ControllerTextConfig.REQUIRE_WAY_FINDER);
        Objects.requireNonNull(dynamicView, ControllerTextConfig.REQUIRE_DYNAMIC_VIEW);

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

