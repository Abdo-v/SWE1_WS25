package client.view;

import client.model.GameState;

import java.util.Objects;

public class DynamicCLIGameView implements client.observer.util.Observer {

    private static final long RENDER_DEBOUNCE_MILLIS = 15L;
    private final CLIDebouncedRenderScheduler renderScheduler = new CLIDebouncedRenderScheduler(RENDER_DEBOUNCE_MILLIS);
    private final DynamicCLIFrameRenderer frameRenderer = new DynamicCLIFrameRenderer();

    private GameState currentGameState;
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
        this.renderScheduler.cancelPending();
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

        renderScheduler.schedule(this::renderIfActive);
    }

    private void renderIfActive() {
        if (this.dynamicModeActive && this.currentGameState != null) {
            render();
        }
    }

    private void render() {
        DynamicCLIFrameRenderer.RenderedCLIFrame frame = frameRenderer.render(this.currentGameState);
        if (frame.lineCount() <= 0) {
            return;
        }
        System.out.print(frame.text());
        System.out.flush(); // Ensure all output is written
    }
}