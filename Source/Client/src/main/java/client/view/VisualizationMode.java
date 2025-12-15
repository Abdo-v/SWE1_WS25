package client.view;

import client.model.GameMode;

public enum VisualizationMode {
    DYNAMIC,
    REDUCED;

    public static VisualizationMode fromGameMode(GameMode mode) {
        if (mode != null && mode.isReduced()) {
            return REDUCED;
        }
        return DYNAMIC;
    }
}
