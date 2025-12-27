package client.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import client.exception.FullMapNotAvailableException;
import client.model.GameState;

/**
 * Unit tests for {@link FullMapWaitService}.
 */
class FullMapWaitServiceTest {

    @Test
    void waitForFullMap_whenFullMapAlreadyAvailable_returnsWithoutSleeping() throws Exception {
        GameStateSynchronizer synchronizer = mock(GameStateSynchronizer.class);
        GameStateQueryService queryService = mock(GameStateQueryService.class);
        when(queryService.isFullMapAvailable()).thenReturn(true);

        FullMapWaitService service = new FullMapWaitService(synchronizer, queryService);
        service.waitForFullMap(new GameState("gs-1"), Duration.ofSeconds(1), Duration.ofMillis(1));

        verify(synchronizer, times(1)).synchronize(org.mockito.ArgumentMatchers.any());
        verify(queryService, times(1)).isFullMapAvailable();
    }

    @Test
    void waitForFullMap_whenTimeoutIsZero_throwsImmediatelyWithZeroAttempts() {
        GameStateSynchronizer synchronizer = mock(GameStateSynchronizer.class);
        GameStateQueryService queryService = mock(GameStateQueryService.class);

        FullMapWaitService service = new FullMapWaitService(synchronizer, queryService);

        FullMapNotAvailableException ex = assertThrows(
                FullMapNotAvailableException.class,
                () -> service.waitForFullMap(new GameState("gs-2"), Duration.ZERO, Duration.ofMillis(1))
        );

        assertEquals("gs-2", ex.getGameStateId());
        assertEquals(0, ex.getAttempts());
    }

    @Test
    void waitForFullMap_whenThreadInterrupted_throwsAndKeepsInterruptedFlag() throws Exception {
        GameStateSynchronizer synchronizer = mock(GameStateSynchronizer.class);
        GameStateQueryService queryService = mock(GameStateQueryService.class);
        when(queryService.isFullMapAvailable()).thenReturn(false);

        FullMapWaitService service = new FullMapWaitService(synchronizer, queryService);

        try {
            Thread.currentThread().interrupt();

            FullMapNotAvailableException ex = assertThrows(
                    FullMapNotAvailableException.class,
                    () -> service.waitForFullMap(new GameState("gs-3"), Duration.ofSeconds(2), Duration.ofSeconds(1))
            );

            assertEquals("gs-3", ex.getGameStateId());
            assertEquals(1, ex.getAttempts());
            org.junit.jupiter.api.Assertions.assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            // Clear interruption for subsequent tests.
            Thread.interrupted();
        }
    }
}
