package client.view;

import client.exception.GameCommunicationException;
import client.model.GameMode;

import java.util.Objects;

public class ClientStartupView {

    public void showAutoFetchGameIdStart() {
        System.out.println("🔍 Auto-fetching game ID from server (ATTR mode)...");
    }

    public void showAutoFetchGameIdResult(String gameId) {
        System.out.println("✅ Retrieved game ID: " + gameId);
    }

    public void showStartupBanner(String serverBaseUrl, String gameId, String gameMode) {
        showStartupBanner(serverBaseUrl, gameId, GameMode.fromCLIValue(gameMode));
    }

    public void showStartupBanner(String serverBaseUrl, String gameId, GameMode gameMode) {
        System.out.println("🎮 GAME CLIENT STARTING");
        System.out.println("========================");
        System.out.println("📡 Server: " + serverBaseUrl);
        System.out.println("🎯 Game ID: " + gameId);
        System.out.println("🖥️  Mode: " + describeMode(gameMode));
        System.out.println("please note the following:");
        System.out.println("use TR game mode for terminal dynamic mode (map real time update)");
        System.out.println("use TRR game mode for reduced representation game flow (no map, moves represented in text lines)");
        System.out.println("use ATTR game mode for auto-fetch terminal mode (game ID fetched automatically)");
    }

    public void showRegisteringPlayer() {
        System.out.println("👤 Registering player...");
    }

    public void showPlayerRegistered(String playerId) {
        System.out.println("🎮 Successfully registered player with ID: " + playerId);
    }

    public void showWaitingForGameStateAfterRegister() {
        System.out.println("Waiting for game state after register...");
    }

    public void showGeneratingAndSendingHalfMap() {
        System.out.println("Generating and sending half map...");
    }

    public void showStartingMainGameLoop() {
        System.out.println("Starting main game loop...");
    }

    public void showConfigurationError(String helpMessage) {
        System.err.println(helpMessage);
    }

    public void showCommunicationError(GameCommunicationException e) {
        System.err.println("🌐 Network Communication Error: " + e.getMessage());
        if (e.isRecoverable()) {
            System.err.println("💡 This error might be temporary. Try:");
            System.err.println("   • Check your internet connection");
            System.err.println("   • Verify the server URL: " + e.getServerUrl());
            System.err.println("   • Wait a moment and restart the application");
        } else {
            System.err.println("❌ This appears to be a permanent issue:");
            System.err.println("   • Verify server availability");
            System.err.println("   • Check if the game ID is valid");
            System.err.println("   • Contact support if the problem persists");
        }
    }

    public void showUnexpectedError(Exception e) {
        System.err.println("❌ Unexpected Error: " + e.getMessage());
        e.printStackTrace(System.err);
    }

    private String describeMode(GameMode gameMode) {
        GameMode safeMode = Objects.requireNonNullElse(gameMode, GameMode.UNKNOWN);
        return switch (safeMode) {
            case TR -> "Terminal";
            case TRR -> "Terminal Reduced";
            case ATTR -> "Auto-Fetch Terminal";
            default -> safeMode.cliValue();
        };
    }
}
