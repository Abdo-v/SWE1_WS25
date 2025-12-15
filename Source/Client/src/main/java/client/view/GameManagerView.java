package client.view;

import client.model.Direction;
import client.model.PlayerState;
import client.model.PlayerStatus;
import client.model.StaticColors;

public class GameManagerView {

    public void showMapValidationFailed(String errorMessages) {
        System.err.println("❌ Map validation failed:");
        System.err.println(errorMessages);
    }

    public void showMapValidationOk() {
        System.out.println(StaticColors.GREEN + "✅ Map validation found no errors, sending map..." + StaticColors.RESET);
    }

    public void showDynamicModeStarting() {
        System.out.println("\n🎮 Starting game with dynamic visualization...");
    }

    public void showMoveSent(Direction direction) {
        System.out.print("Move " + direction.name() + " sent to server, ");
    }

    public void showPosition(String positionText) {
        System.out.print(positionText);
    }

    public void showAiError(String message) {
        System.err.println("🤖 AI Error: " + message);
    }

    public void showNetworkError(String message) {
        System.err.println("🌐 Network Error: " + message);
    }

    public void showWon(PlayerState playerState, int loops, boolean showLoops) {
        System.out.println("🎉🎉🎉======= = = YOU WON! = = =======🎉🎉🎉");
        System.out.println(playerState);
        if (showLoops) {
            System.out.println(" loops: " + loops);
        }
    }

    public void showLost(PlayerState playerState, int loops, boolean showLoops) {
        System.out.println("💀💀💀======= = = YOU LOST! = = =======💀💀💀");
        System.out.println(playerState);
        if (showLoops) {
            System.out.println(" loops: " + loops);
        }
    }

    public void showUnhandledStatus(PlayerStatus status) {
        System.err.println("Unhandled player state: " + status + ". Exiting game.");
    }

    public void showLoops(int loops) {
        System.out.println(" loops: " + loops);
    }

    public void showMapError(String recoveryMessage) {
        System.err.println("🗺️ Map Error: " + recoveryMessage);
    }
}
