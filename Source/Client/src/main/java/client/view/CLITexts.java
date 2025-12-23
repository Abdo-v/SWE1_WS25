package client.view;

/**
 * Central place for repeated CLI text fragments.
 *
 * <p>Package-private to avoid Windows case-only rename problems while still
 * keeping the class name consistently spelled as "CLI".
 */
final class CLITexts {

    private CLITexts() {
    }

    static final String SEPARATOR_SHORT = "--------------------";
    static final String SEPARATOR_HALF_MAP = "-------------------------";

    static final String GAME_STATE_HEADER = "--- Game State ---";
    static final String GAME_STATE_FOOTER = "--------------------";

    static final String MAP_NOT_AVAILABLE = "Map data not available.";
    static final String MAP_DIMENSIONS_INVALID = "Map dimensions are invalid (max indices are negative).";
}
