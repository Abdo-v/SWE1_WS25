package client.view;

import client.model.GameState;

import java.util.Objects;
import java.util.Optional;

/**
 * Dynamic, emoji-based CLI visualization of the current {@link GameState}.
 *
 * <p>Rendering is debounced to keep the terminal responsive when multiple updates arrive in a
 * short time window (e.g., model synchronization).
 */
public class DynamicCLIGameView implements client.observer.util.Observer {

    private static final long RENDER_DEBOUNCE_MILLIS = 15L;
    private final CLIDebouncedRenderScheduler renderScheduler = new CLIDebouncedRenderScheduler(RENDER_DEBOUNCE_MILLIS);
    private final DynamicCLIFrameRenderer frameRenderer = new DynamicCLIFrameRenderer();

    private Optional<GameState> currentGameState = Optional.empty();
    private boolean dynamicModeActive = false;

    /**
     * Enables dynamic rendering of the game view.
     * Subsequent calls to update will render the game state.
     */
    public void enableDynamicMode() {
        this.dynamicModeActive = true;
        this.currentGameState.ifPresent(ignored -> render());
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
        this.currentGameState = Optional.of(Objects.requireNonNull(gameState, "gameState is required"));
        requestRender();
    }

    /**
     * Schedules a render shortly in the future.
     * If multiple updates arrive quickly (e.g., during bulk model updates), renders are coalesced.
     */
    public void requestRender() {
        if (!this.dynamicModeActive || this.currentGameState.isEmpty()) {
            return;
        }

        renderScheduler.schedule(this::renderIfActive);
    }

    private void renderIfActive() {
        if (this.dynamicModeActive) {
            this.currentGameState.ifPresent(ignored -> render());
        }
    }

    private void render() {
        GameState state = this.currentGameState.orElseThrow();
        DynamicCLIFrameRenderer.RenderedCLIFrame frame = frameRenderer.render(state);
        if (frame.lineCount() <= 0) {
            return;
        }
        System.out.print(frame.text());
        System.out.flush(); // Ensure all output is written
    }
}