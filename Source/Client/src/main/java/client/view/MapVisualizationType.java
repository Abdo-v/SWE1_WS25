package client.view;

import java.util.Objects;
import java.util.Locale;

public enum MapVisualizationType {
    OWN("own"),
    OPPONENT("opponent"),
    FULL("full"),
    UNKNOWN("unknown");

    private final String cliValue;

    MapVisualizationType(String cliValue) {
        this.cliValue = cliValue;
    }

    public String cliValue() {
        return cliValue;
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
