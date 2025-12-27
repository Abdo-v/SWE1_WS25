package client.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link AIInvariantViolationException} fail-fast behavior and optional context fields.
 */
class AIInvariantViolationExceptionTest {

    @Test
    void messageOnlyConstructor_setsMessageAndLeavesContextEmpty() {
        AIInvariantViolationException ex = new AIInvariantViolationException("boom");

        assertEquals("boom", ex.getMessage());
        assertTrue(ex.getAiComponent().isEmpty());
        assertTrue(ex.getInvariant().isEmpty());
        assertTrue(ex.getDetails().isEmpty());
    }

    @Test
    void messageAndCauseConstructor_setsCauseAndLeavesContextEmpty() {
        IllegalStateException cause = new IllegalStateException("root");
        AIInvariantViolationException ex = new AIInvariantViolationException("boom", cause);

        assertEquals("boom", ex.getMessage());
        assertSame(cause, ex.getCause());
        assertTrue(ex.getAiComponent().isEmpty());
        assertTrue(ex.getInvariant().isEmpty());
        assertTrue(ex.getDetails().isEmpty());
    }

    @Test
    void detailedConstructor_populatesContextOptionals() {
        AIInvariantViolationException ex = new AIInvariantViolationException(
                "boom",
                "WayFinderLogic",
                "neighbor direction must be present",
                "current=(x,y)"
        );

        assertEquals("boom", ex.getMessage());
        assertEquals("WayFinderLogic", ex.getAiComponent().orElseThrow());
        assertEquals("neighbor direction must be present", ex.getInvariant().orElseThrow());
        assertEquals("current=(x,y)", ex.getDetails().orElseThrow());
    }
}
