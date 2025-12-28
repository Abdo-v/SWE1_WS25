package client.view;

import java.util.Objects;
import java.util.Locale;

/**
 * Selects which subset of the map should be shown by snapshot-style CLI visualization.
 *
 * <p>Values are intentionally mapped from simple CLI strings; unknown inputs resolve to
 * {@link #UNKNOWN} to keep argument handling robust.
 */
public enum MapVisualizationType {
    OWN("own"),
    OPPONENT("opponent"),
    FULL("full"),
    UNKNOWN("unknown");

    private final String cliValue;

    MapVisualizationType(String cliValue) {
        this.cliValue = cliValue;
    }

    public static MapVisualizationType fromCLIValue(String value) {
        String normalized = Objects.requireNonNullElse(value, "").trim().toLowerCase(Locale.ROOT);
        for (MapVisualizationType type : values()) {
            if (type.cliValue.equals(normalized)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
