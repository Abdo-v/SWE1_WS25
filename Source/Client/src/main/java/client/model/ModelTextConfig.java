package client.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Centralized text/config constants for the model layer.
 *
 * <p>This keeps model/AI/mapper code free of scattered string literals while preserving
 * existing runtime behavior.
 */
public final class ModelTextConfig {

    private ModelTextConfig() {
    }

    // Generic "required" messages
    public static final String REQUIRE_THROWABLE = "throwable is required";
    public static final String REQUIRE_EVENT_TYPE = "event type is required";
    public static final String REQUIRE_EVENT = "event is required";
    public static final String REQUIRE_GAME_STATE_ID = "game state ID is required";
    public static final String REQUIRE_GAME_STATE = "game state is required";
    public static final String REQUIRE_MAP = "map is required";
    public static final String REQUIRE_OPPONENT_FORT_POSITION = "opponent fort position is required";
    public static final String REQUIRE_TREASURE_POSITION = "treasure position is required";
    public static final String REQUIRE_PLAYER = "player is required";
    public static final String REQUIRE_SOURCE_GAME_STATE = "source game state is required";
    public static final String REQUIRE_OBSERVER = "observer is required";
    public static final String REQUIRE_HALF_MAP_RULES = "halfMapRules";
    public static final String REQUIRE_CROSS_HALF_MAP_RULES = "crossHalfMapRules";
    public static final String REQUIRE_PHASE = "phase";

    // AI/mapper argument requirement messages
    public static final String REQUIRE_GAME_MAP = "GameMap is required";
    public static final String REQUIRE_CURRENT_POSITION = "Current position is required";

    // Short required labels (kept as-is for existing NPE messages)
    public static final String REQUIRE_ORIENTATION = "orientation";
    public static final String REQUIRE_NODES = "nodes";
    public static final String REQUIRE_NODE = "node";
    public static final String REQUIRE_INCLUDE_NODE = "includeNode";

    // Mapper / validation messages
    public static final String ERROR_ORIENTATION_REQUIRED = "Map orientation must be set";
    public static final String ERROR_MAX_XY_NON_NEGATIVE = "maxX/maxY must be non-negative";
    public static final String ERROR_INVALID_COORDINATES_PREFIX = "Invalid coordinates: (";
    public static final String ERROR_INVALID_COORDINATES_MIDDLE = ", ";
    public static final String ERROR_INVALID_COORDINATES_SUFFIX = "), not found in gameMap";

    public static final String ERROR_HALF_MAP_TOO_MANY_NODES_PREFIX = "Cannot add more than ";
    public static final String ERROR_HALF_MAP_TOO_MANY_NODES_SUFFIX = " map nodes to a half map.";

    public static final String ERROR_TILE_COUNTS_NON_NEGATIVE = "Tile counts must be non-negative";
    public static final String ERROR_MIN_MOUNTAIN_LE_MAX = "minMountainTiles must be <= maxMountainTiles";
    public static final String ERROR_MIN_WATER_LE_MAX = "minWaterTiles must be <= maxWaterTiles";
    public static final String ERROR_FORT_TILES_POSITIVE = "fortTiles must be positive";
    public static final String ERROR_MIN_BORDER_RATIO_RANGE = "minBorderWalkableRatio must be in (0, 1]";
    public static final String ERROR_WATER_ATTEMPT_MULTIPLIER_POSITIVE = "waterPlacementAttemptMultiplier must be positive";

    public static final String ERROR_WIDTH_HEIGHT_POSITIVE = "Width and height must be positive";
    public static final String ERROR_MAP_GENERATION_FAILED_UNEXPECTED = "Map generation failed unexpectedly";

    public static final String ERROR_INVALID_ORIENTATION_PREFIX = "Invalid orientation: ";

    public static final String ERROR_PLAYER_HALF_MAP_REQUIRED = "PlayerHalfMap must be provided.";
    public static final String LOG_OBSERVER_THREW_DURING_UPDATE = "Observer threw during update (type={})";

    public static String invalidCoordinatesMessage(int x, int y) {
        return ERROR_INVALID_COORDINATES_PREFIX + x + ERROR_INVALID_COORDINATES_MIDDLE + y + ERROR_INVALID_COORDINATES_SUFFIX;
    }

    public static String tooManyHalfMapNodesMessage(int maxNodes) {
        return ERROR_HALF_MAP_TOO_MANY_NODES_PREFIX + maxNodes + ERROR_HALF_MAP_TOO_MANY_NODES_SUFFIX;
    }

    public static <T> T defaultIfMissing(T value, T defaultValue) {
        try {
            return Objects.requireNonNull(value);
        } catch (NullPointerException e) {
            return defaultValue;
        }
    }

    public static <T> Optional<T> optionalIfPresent(T value) {
        try {
            return Optional.of(Objects.requireNonNull(value));
        } catch (NullPointerException e) {
            return Optional.empty();
        }
    }

    public static String safeStringOrDefault(String value, String defaultValue) {
        return defaultIfMissing(value, defaultValue);
    }
}
