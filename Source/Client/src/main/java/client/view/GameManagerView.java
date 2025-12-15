package client.view;

import client.model.Direction;
import client.model.PlayerState;
import client.model.PlayerStatus;
import client.model.StaticColors;

public class GameManagerView implements GameOutput {

    @Override
    public void showMapValidationFailed(String errorMessages) {
        System.err.println("❌ Map validation failed:");
        System.err.println(errorMessages);
    }

    @Override
    public void showMapValidationOk() {
        System.out.println(StaticColors.GREEN + "✅ Map validation found no errors, sending map..." + StaticColors.RESET);
    }

    @Override
    public void showDynamicModeStarting() {
        System.out.println("\n🎮 Starting game with dynamic visualization...");
    }

    @Override
    public void showMoveSent(Direction direction) {
        System.out.print("Move " + direction.name() + " sent to server, ");
    }

    @Override
    public void showPosition(String positionText) {
        System.out.print(positionText);
    }

    @Override
    public void showAiError(String message) {
        System.err.println("🤖 AI Error: " + message);
    }

    @Override
    public void showNetworkError(String message) {
        System.err.println("🌐 Network Error: " + message);
    }

    @Override
    public void showWon(PlayerState playerState, int loops, boolean showLoops) {
        System.out.println("🎉🎉🎉======= = = YOU WON! = = =======🎉🎉🎉");
        System.out.println(playerState);
        if (showLoops) {
            System.out.println(" loops: " + loops);
        }
    }

    @Override
    public void showLost(PlayerState playerState, int loops, boolean showLoops) {
        System.out.println("💀💀💀======= = = YOU LOST! = = =======💀💀💀");
        System.out.println(playerState);
        if (showLoops) {
            System.out.println(" loops: " + loops);
        }
    }

    @Override
    public void showUnhandledStatus(PlayerStatus status) {
        System.err.println("Unhandled player state: " + status + ". Exiting game.");
    }

    @Override
    public void showLoops(int loops) {
        System.out.println(" loops: " + loops);
    }

    @Override
    public void showMapError(String recoveryMessage) {
        System.err.println("🗺️ Map Error: " + recoveryMessage);
    }
}
