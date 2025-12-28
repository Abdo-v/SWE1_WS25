package client.exception;

/**
 * Shared text fragments for exception messages and user-facing recovery hints.
 */
final class TextCnofig {

    private TextCnofig() {
        // utility class
    }

    public static final String UNKNOWN_TEXT = "Unknown";
    public static final String UNKNOWN_LOWER_TEXT = "unknown";
    public static final String NOT_SPECIFIED_TEXT = "Not specified";

    public static final String REQUIRE_MESSAGE_IS_REQUIRED = "message is required";
    public static final String REQUIRE_SUGGESTED_FALLBACK_DIRECTION_IS_REQUIRED = "suggestedFallbackDirection is required";
    public static final String REQUIRE_GAME_STATE_ID_IS_REQUIRED = "gameStateId is required";
    public static final String REQUIRE_OPERATION_IS_REQUIRED = "operation is required";
    public static final String REQUIRE_CURRENT_STATE_IS_REQUIRED = "currentState is required";
    public static final String REQUIRE_EXPECTED_STATE_IS_REQUIRED = "expectedState is required";
    public static final String REQUIRE_MAP_TYPE_IS_REQUIRED = "mapType is required";
    public static final String REQUIRE_PROCESSING_STAGE_IS_REQUIRED = "processingStage is required";
    public static final String REQUIRE_COORDINATE_CONTEXT_IS_REQUIRED = "coordinateContext is required";

    public static final String PREFIX_AI_DECISION_FAILURE = "AI Decision Failure: ";
    public static final String PREFIX_GAME_STATE_ERROR = "Game State Error: ";
    public static final String PREFIX_MAP_PROCESSING_ERROR = "Map Processing Error: ";

    public static final String DEBUG_REPORT_AI_HEADER = "=== AI DECISION EXCEPTION DEBUG REPORT ===\n";
    public static final String DEBUG_REPORT_GAME_STATE_HEADER = "=== GAME STATE EXCEPTION DEBUG REPORT ===\n";
    public static final String DEBUG_REPORT_END = "=== END DEBUG REPORT ===";
    public static final String DEBUG_LABEL_MESSAGE = "Message: ";
    public static final String DEBUG_LABEL_AI_COMPONENT = "AI Component: ";
    public static final String DEBUG_LABEL_DECISION_CONTEXT = "Decision Context: ";
    public static final String DEBUG_LABEL_HAS_GAME_STATE_SNAPSHOT = "Has Game State Snapshot: ";
    public static final String DEBUG_YES = "Yes";
    public static final String DEBUG_NO = "No";
    public static final String DEBUG_LABEL_UNDERLYING_CAUSE = "Underlying Cause: ";
    public static final String DEBUG_CAUSE_SEPARATOR = " - ";
    public static final String DEBUG_LABEL_STACK_TRACE_AVAILABLE = "Stack Trace: Available via printStackTrace()";

    public static final String DEBUG_LABEL_GAME_STATE_ID = "Game State ID: ";
    public static final String DEBUG_LABEL_FAILED_OPERATION = "Failed Operation: ";
    public static final String DEBUG_LABEL_CURRENT_STATE = "Current State: ";
    public static final String DEBUG_LABEL_EXPECTED_STATE = "Expected State: ";
    public static final String DEBUG_LABEL_TIMESTAMP = "Timestamp: ";

    public static final String USER_GAME_STATE_PREFIX = "🎮 Game State Error: ";
    public static final String USER_DURING_OPERATION_PREFIX = "📋 During operation: ";
    public static final String USER_EXPECTED_STATE_PREFIX = "⚠️  Expected state: ";
    public static final String USER_EXPECTED_STATE_SEPARATOR = ", but found: ";
    public static final String USER_INTERNAL_ERROR_HINT = "💡 This indicates an internal error. Please try restarting the game.";

    public static final String LABEL_BRACKET_OPEN = " [";
    public static final String LABEL_BRACKET_CLOSE = "]";

    public static final String LABEL_COMPONENT = "Component: ";
    public static final String LABEL_CONTEXT = "Context: ";
    public static final String LABEL_GAME = "Game: ";
    public static final String LABEL_OPERATION = "Operation: ";
    public static final String LABEL_SERVER = "Server: ";
    public static final String LABEL_HTTP_STATUS = "HTTP Status: ";

    public static final String LABEL_MAP_TYPE = "Map Type: ";
    public static final String LABEL_STAGE = "Stage: ";
    public static final String LABEL_EXPECTED_NODES = "Expected Nodes: ";
    public static final String LABEL_ACTUAL_NODES = "Actual: ";
    public static final String LABEL_COORDINATES = "Coordinates: ";

    public static final String FULL_MAP_TIMEOUT_PREFIX = "Full map not available within timeout";
    public static final String FULL_MAP_TIMEOUT_LABEL_GAME_STATE_ID = "GameStateId: ";
    public static final String FULL_MAP_TIMEOUT_LABEL_TIMEOUT_MS = "TimeoutMs: ";
    public static final String FULL_MAP_TIMEOUT_LABEL_ATTEMPTS = "Attempts: ";
    public static final String FALLBACK_UNKNOWN_ID = "unknown";

    public static final String MAP_RECOVERY_PREFIX = "Map processing failed: ";
    public static final String MAP_RECOVERY_MAP_TYPE_LINE = "Map Type: ";
    public static final String MAP_RECOVERY_STAGE_LINE = "Stage: ";
    public static final String MAP_RECOVERY_COORDINATES_LINE = "Coordinates: ";
    public static final String MAP_RECOVERY_NODES_PREFIX = "Nodes: expected ";
    public static final String MAP_RECOVERY_NODES_SEPARATOR = ", received ";
    public static final String MAP_RECOVERY_ACTION_RECOVERABLE = "Action: continuing with retry/polling (recoverable).\n";
    public static final String MAP_RECOVERY_ACTION_NOT_RECOVERABLE = "Action: aborting this run (not recoverable).\n";
    public static final String MAP_RECOVERY_HINT_DEBUG_TRACES_ROOT_CAUSE = "Hint: enable debug traces via -Dclient.debug=true if you need the root cause.";
    public static final String MAP_RECOVERY_HINT_PROTOCOL_COMPAT = "Hint: check protocol/business-rule compatibility and server ResponseEnvelope exceptionName/exceptionMessage.\n";
    public static final String MAP_RECOVERY_HINT_DEBUG_TRACES_STACK = "Hint: enable debug traces via -Dclient.debug=true to print stack traces.";
}
