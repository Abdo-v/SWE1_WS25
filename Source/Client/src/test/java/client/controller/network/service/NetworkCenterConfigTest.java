package client.controller.network.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link NetworkCenterConfig}.
 *
 * <p>Validates configuration defaults and input validation.
 */
class NetworkCenterConfigTest {

    /**
     * Ensures the default configuration uses the intended poll delay.
     */
    @Test
    void defaultConfig_usesExpectedDelay() {
        NetworkCenterConfig config = NetworkCenterConfig.defaultConfig();
        assertEquals(400, config.pollGameStateDelayMillis());
    }

    /**
     * Ensures negative poll delays are rejected.
     */
    @Test
    void constructor_whenNegativeDelay_throws() {
        assertThrows(IllegalArgumentException.class, () -> new NetworkCenterConfig(-1));
    }
}
