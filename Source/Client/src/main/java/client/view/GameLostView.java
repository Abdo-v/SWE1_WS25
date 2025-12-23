package client.view;

import client.model.PlayerState;

import java.io.PrintStream;
import java.util.Objects;

/**
 * View responsible for printing the "game lost" screen.
 */
public final class GameLostView {

    private final PrintStream out;
    private final PlayerSummaryFormatter playerSummaryFormatter;

    public GameLostView(PrintStream out) {
        this.out = Objects.requireNonNull(out, "out must not be null");
        this.playerSummaryFormatter = new PlayerSummaryFormatter();
    }

    public void show(PlayerState playerState, int movesMade) {
        Objects.requireNonNull(playerState, "playerState must not be null");

        out.println("\n========================================");
        out.println("💀💀💀  YOU LOST  💀💀💀");
        out.println("----------------------------------------");
        out.print(playerSummaryFormatter.format(playerState, movesMade));
        out.println("========================================");
    }
}
