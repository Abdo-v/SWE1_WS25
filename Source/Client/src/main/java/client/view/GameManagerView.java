package client.view;

import client.model.Direction;
import client.model.PlayerState;
import client.model.PlayerStatus;

import java.io.PrintStream;
import java.util.Objects;

public class GameManagerView implements GameOutput {

    private final PrintStream out;
    private final PrintStream err;
    private final GameWonView gameWonView;
    private final GameLostView gameLostView;

    public GameManagerView() {
        this(System.out, System.err);
    }

    public GameManagerView(PrintStream out, PrintStream err) {
        this.out = Objects.requireNonNull(out, "out must not be null");
        this.err = Objects.requireNonNull(err, "err must not be null");
        this.gameWonView = new GameWonView(this.out);
        this.gameLostView = new GameLostView(this.out);
    }

    @Override
    public void showMapValidationFailed(String errorMessages) {
        err.println("❌ Map validation failed:");
        err.println(errorMessages);
    }

    @Override
    public void showMapValidationOk() {
        out.println("✅ Map validation found no errors, sending map...");
    }

    @Override
    public void showDynamicModeStarting() {
        out.println("\n🎮 Starting game with dynamic visualization...");
    }

    @Override
    public void showMoveSent(Direction direction) {
        out.print("Move " + direction.name() + " sent to server, ");
    }

    @Override
    public void showPosition(String positionText) {
        out.print(positionText);
    }

    @Override
    public void showAiError(String message) {
        err.println("🤖 AI Error: " + message);
    }

    @Override
    public void showNetworkError(String message) {
        err.println("🌐 Network Error: " + message);
    }

    @Override
    public void showWon(PlayerState playerState, int loops, boolean showLoops) {
        // Keep the signature for wiring compatibility; present user-friendly info.
        gameWonView.show(Objects.requireNonNull(playerState, "playerState must not be null"), loops);
    }

    @Override
    public void showLost(PlayerState playerState, int loops, boolean showLoops) {
        // Keep the signature for wiring compatibility; present user-friendly info.
        gameLostView.show(Objects.requireNonNull(playerState, "playerState must not be null"), loops);
    }

    @Override
    public void showUnhandledStatus(PlayerStatus status) {
        err.println("Unhandled player state: " + status + ". Exiting game.");
    }

    @Override
    public void showLoops(int loops) {
        out.println(" loops: " + loops);
    }

    @Override
    public void showMapError(String recoveryMessage) {
        err.println("🗺️ Map Error: " + recoveryMessage);
    }
}
