package client.view;

import client.model.GameMode;

import java.util.Objects;

public enum VisualizationMode {
    DYNAMIC,
    REDUCED;

    public static VisualizationMode fromGameMode(GameMode mode) {
        GameMode safeMode = Objects.requireNonNullElse(mode, GameMode.UNKNOWN);
        if (safeMode.isReduced()) {
            return REDUCED;
        }
        return DYNAMIC;
    }
}
