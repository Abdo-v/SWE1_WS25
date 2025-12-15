package client.model;

import java.util.Locale;

public enum GameMode {
    TR("TR"),
    TRR("TRR"),
    ATTR("ATTR"),
    UNKNOWN("UNKNOWN");

    private final String cliValue;

    GameMode(String cliValue) {
        this.cliValue = cliValue;
    }

    public String cliValue() {
        return cliValue;
    }

    public boolean isReduced() {
        return this == TRR;
    }

    public boolean usesAutoFetchGameId() {
        return this == ATTR;
    }

    public boolean isDynamicVisualization() {
        return this == TR || this == ATTR;
    }

    public static GameMode fromCliValue(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (GameMode mode : values()) {
            if (mode.cliValue.equals(normalized)) {
                return mode;
            }
        }
        return UNKNOWN;
    }
}
