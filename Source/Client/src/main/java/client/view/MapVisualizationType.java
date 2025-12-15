package client.view;

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

    public static MapVisualizationType fromCliValue(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (MapVisualizationType type : values()) {
            if (type.cliValue.equals(normalized)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
