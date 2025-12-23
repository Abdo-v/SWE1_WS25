package client.controller;

import java.time.Duration;

/**
 * Central place for controller-layer polling defaults.
 */
public final class PollingDefaults {

    private PollingDefaults() {
        // utility class
    }

    /** Default timeout to wait for the server full map. */
    public static final Duration FULL_MAP_WAIT_TIMEOUT = Duration.ofSeconds(20);

    /**
     * Minimum (and default) polling interval to avoid hammering the server.
     * Requirement: never poll more often than every 500ms.
     */
    public static final Duration MIN_POLL_INTERVAL = Duration.ofMillis(500);

    /** Default polling interval used by the client when waiting. */
    public static final Duration DEFAULT_POLL_INTERVAL = MIN_POLL_INTERVAL;
}
