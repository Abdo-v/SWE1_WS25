package client.controller.network.service;

/**
 * Central configuration for {@link NetworkCenter}.
 *
 * <p>Extracted to avoid magic numbers (e.g. poll delays) scattered across networking code.
 */
public record NetworkCenterConfig(
        long pollGameStateDelayMillis
) {

    public NetworkCenterConfig {
        if (pollGameStateDelayMillis < 0) {
            throw new IllegalArgumentException("pollGameStateDelayMillis must be >= 0");
        }
    }

    public static NetworkCenterConfig defaultConfig() {
        return new NetworkCenterConfig(400);
    }
}
