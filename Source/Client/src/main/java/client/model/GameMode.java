package client.model;

import java.util.Locale;

/**
 * CLI-selectable client runtime mode.
 *
 * <p>The mode affects visualization (dynamic vs reduced) and whether the game id is expected
 * as an argument or fetched automatically.
 */
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

    public boolean isDynamicVisualization() {
        return this == TR || this == ATTR;
    }

    public static GameMode fromCLIValue(String value) {
        final String normalized;
        try {
            normalized = value.trim().toUpperCase(Locale.ROOT);
        } catch (NullPointerException e) {
            return UNKNOWN;
        }

        for (GameMode mode : values()) {
            if (mode.cliValue.equals(normalized)) {
                return mode;
            }
        }
        return UNKNOWN;
    }
}
