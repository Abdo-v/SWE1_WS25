package client.model;

import java.util.Locale;
import java.util.Optional;

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
        return Optional.ofNullable(value)
                .map(v -> v.trim().toUpperCase(Locale.ROOT))
                .flatMap(normalized -> {
                    for (GameMode mode : values()) {
                        if (mode.cliValue.equals(normalized)) {
                            return Optional.of(mode);
                        }
                    }
                    return Optional.empty();
                })
                .orElse(UNKNOWN);
    }
}
