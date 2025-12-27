package client.view;

import client.exception.ConfigurationException;
import client.exception.FullMapNotAvailableException;
import client.exception.GameCommunicationException;
import client.view.testsupport.StdIoCapture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientStartupViewTest {

    private static final String ANSI_ESCAPE_PREFIX = "\u001B[";

    @Test
    void showStartupBanner_writesToStdoutOnly_andHasNoAnsiEscapes() {
        try (StdIoCapture io = new StdIoCapture()) {
            new ClientStartupView().showStartupBanner("http://server", "game-123", "TR");

            String out = io.stdout();
            String err = io.stderr();

            assertTrue(out.contains("GAME CLIENT STARTING"));
            assertTrue(out.contains("Server: http://server"));
            assertTrue(out.contains("Game ID: game-123"));
            assertTrue(out.contains("Mode:"));
            assertEquals("", err);

            assertFalse(out.contains(ANSI_ESCAPE_PREFIX));
        }
    }

    @Test
    void showConfigurationError_writesToStderrOnly_andIncludesContext() {
        try (StdIoCapture io = new StdIoCapture()) {
            ConfigurationException ex = new ConfigurationException(
                    "Invalid game mode provided",
                    "gameMode",
                    "XYZ",
                    new String[]{"TR", "TRR", "ATTR"}
            );

            new ClientStartupView().showConfigurationError(ex);

            assertEquals("", io.stdout());

            String err = io.stderr();
            assertTrue(err.contains("Configuration Error:"));
            assertTrue(err.contains("Parameter: gameMode"));
            assertTrue(err.contains("Provided: XYZ"));
            assertTrue(err.contains("Valid options: TR, TRR, ATTR"));

            assertFalse(err.contains(ANSI_ESCAPE_PREFIX));
        }
    }

    @Test
    void showCommunicationError_recoverable_printsRetryHintToStderr() {
        try (StdIoCapture io = new StdIoCapture()) {
            GameCommunicationException ex = new GameCommunicationException(
                    "boom",
                    "http://server",
                    "registerPlayer",
                    503,
                    "SomeRemoteException",
                    "remote details"
            );

            new ClientStartupView().showCommunicationError(ex);

            assertEquals("", io.stdout());

            String err = io.stderr();
            assertTrue(err.contains("Communication failed"));
            assertTrue(err.contains("Operation: registerPlayer"));
            assertTrue(err.contains("Server: http://server"));
            assertTrue(err.contains("HTTP Status: 503"));
            assertTrue(err.contains("Server Error: SomeRemoteException"));
            assertTrue(err.contains("Details: remote details"));
            assertTrue(err.contains("Potentially recoverable"));

            assertFalse(err.contains(ANSI_ESCAPE_PREFIX));
        }
    }

    @Test
    void showFullMapNotAvailable_writesToStderrOnly_andHasNoAnsiEscapes() {
        try (StdIoCapture io = new StdIoCapture()) {
            FullMapNotAvailableException ex = new FullMapNotAvailableException("gs", 1000L, 3);

            new ClientStartupView().showFullMapNotAvailable(ex);

            assertEquals("", io.stdout());

            String err = io.stderr();
            assertTrue(err.contains("Full map not available"));
            assertTrue(err.contains("Tip:"));
            assertFalse(err.contains(ANSI_ESCAPE_PREFIX));
        }
    }
}
