package client.view;

/**
 * Central place for CLI emoji/icon constants.
 *
 * <p>Keeping these in one place avoids duplicates across different views
 * (generation snapshot, full snapshot, dynamic view).
 */
final class CLIIcons {

    private CLIIcons() {
    }

    // Terrain
    public static final String GRASS = "🟩";
    public static final String MOUNTAIN = "⬜";
    public static final String WATER = "🟦";

    // Players
    public static final String PLAYER = "🫤";
    public static final String PLAYER_WITH_TREASURE = "🤑";
    public static final String OPPONENT = "👿";
    public static final String OPPONENT_WITH_TREASURE = "👹";

    // Objectives / Events
    public static final String OWN_FORT = "🏰";
    public static final String OPPONENT_FORT = "🏯";
    public static final String TREASURE = "💎";
    public static final String CLASH = "💥";

    // Fallback
    public static final String UNKNOWN = "❓";
}
