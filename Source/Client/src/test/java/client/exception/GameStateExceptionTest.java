package client.exception;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link GameStateException} verifying message formatting and report generation.
 */
class GameStateExceptionTest {

    @Test
    void getDebugReport_containsKeySectionsAndDoesNotThrow() {
        GameStateException ex = new GameStateException(
                "broken",
                new IllegalArgumentException("cause"),
                "gs-1",
                Operation.MAKE_MOVE,
                FailureReason.ERROR,
            FailureReason.UNKNOWN
        );

        String report = ex.getDebugReport();
        assertTrue(report.contains("GAME STATE EXCEPTION DEBUG REPORT"));
        assertTrue(report.contains("Game State ID"));
        assertTrue(report.contains("Underlying Cause"));
    }

    @Test
    void getUserMessage_whenExpectedStatePresent_mentionsExpectedState() {
        GameStateException ex = new GameStateException(
                "invalid transition",
                "gs-2",
                Operation.MAKE_MOVE,
                FailureReason.ERROR,
            FailureReason.UNKNOWN
        );

        String msg = ex.getUserMessage();
        assertTrue(msg.contains("Expected state"));
    }

    @Test
    void getUserMessage_whenExpectedStateMissing_doesNotMentionExpectedState() {
        GameStateException ex = new GameStateException("oops");
        String msg = ex.getUserMessage();

        assertFalse(msg.contains("Expected state"));
    }
}
