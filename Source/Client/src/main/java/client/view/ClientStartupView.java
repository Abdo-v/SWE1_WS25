package client.view;

import client.exception.ConfigurationException;
import client.exception.FullMapNotAvailableException;
import client.exception.GameCommunicationException;
import client.model.common.DebugSettings;
import client.model.GameMode;

import java.util.Objects;

/**
 * CLI output for the client startup sequence and early failure scenarios.
 *
 * <p>This view prints user-facing progress messages and summarizes exceptions in a way that is
 * helpful during manual runs. Detailed stack traces are delegated to {@link DebugSettings}.
 */
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

    private void showStartupBanner(String serverBaseUrl, String gameId, GameMode gameMode) {
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

    public void showWaitingForFullMap() {
        System.out.println("Waiting for full map from server...");
    }

    public void showFullMapNotAvailable(FullMapNotAvailableException e) {
        Objects.requireNonNull(e, "full map exception is required");
        System.err.println("⏳ Full map not available in time: " + safeText(e.getMessage()));
        System.err.println("💡 Tip: Ensure another player joined and sent their half map.");
        DebugSettings.printStackTraceIfDebug(e);
    }

    public void showStartingMainGameLoop() {
        System.out.println("Starting main game loop...");
    }

    public void showConfigurationError(ConfigurationException e) {
        Objects.requireNonNull(e, "configuration exception is required");

        System.err.println("Configuration Error: " + safeText(e.getMessage()));
        e.getConfigurationKey().ifPresent(key -> System.err.println("Parameter: " + key));
        e.getProvidedValue().ifPresent(value -> System.err.println("Provided: " + value));

        String[] validValues = e.getValidValues();
        if (validValues.length > 0) {
            System.err.println("Valid options: " + String.join(", ", validValues));
        }

        DebugSettings.printStackTraceIfDebug(e);
    }

    public void showCommunicationError(GameCommunicationException e) {
        Objects.requireNonNull(e, "communication exception is required");

        System.err.println("🌐 Communication failed: " + safeText(e.getMessage()));
        e.getOperation().filter(op -> !op.isBlank()).ifPresent(op -> System.err.println("Operation: " + op));
        e.getServerUrl().filter(url -> !url.isBlank()).ifPresent(url -> System.err.println("Server: " + url));
        if (e.getHttpStatusCode() > 0) {
            System.err.println("HTTP Status: " + e.getHttpStatusCode());
        }

        e.getRemoteExceptionName().filter(name -> !name.isBlank()).ifPresent(name -> System.err.println("Server Error: " + name));
        e.getRemoteExceptionMessage().filter(msg -> !msg.isBlank()).ifPresent(msg -> System.err.println("Details: " + msg));

        if (e.isRecoverable()) {
            System.err.println("💡 Potentially recoverable: retry after a short wait.");
        } else {
            System.err.println("💡 Likely a protocol/business-rule issue. Check game ID, player registration, and request validity.");
        }

        DebugSettings.printStackTraceIfDebug(e);
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

    private static String safeText(String text) {
        return Objects.requireNonNullElse(text, "(no message)");
    }
}
