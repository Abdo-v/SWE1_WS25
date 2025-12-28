package client.controller;

import java.util.Objects;
import java.util.Optional;

/**
 * Centralized text/config constants for the controller layer.
 *
 * This keeps controller/service classes free of scattered string literals
 * while preserving existing runtime behavior.
 */
public final class ControllerTextConfig {

    private ControllerTextConfig() {
    }

    public static final String UNKNOWN = "unknown";
    public static final String MISSING = "missing";

    public static final String REQUIRE_STATE = "state is required";
    public static final String REQUIRE_GAME_STATE = "gameState is required";
    public static final String REQUIRE_GAME_STATE_ID = "gameStateId is required";
    public static final String REQUIRE_SERVER_BASE_URL = "serverBaseUrl is required";
    public static final String REQUIRE_OUTPUT = "output is required";
    public static final String REQUIRE_NETWORK_CENTER = "networkCenter is required";
    public static final String REQUIRE_GAME_STATE_SYNCHRONIZER = "gameStateSynchronizer is required";
    public static final String REQUIRE_GAME_STATE_QUERY_SERVICE = "gameStateQueryService is required";
    public static final String REQUIRE_DIRECTION = "direction is required";
    public static final String REQUIRE_ENVELOPE = "envelope is required";

    public static final String REQUIRE_MAP_GENERATOR = "mapGenerator is required";
    public static final String REQUIRE_MAP_VALIDATOR = "mapValidator is required";
    public static final String REQUIRE_MAP_GENERATION_VIEW = "mapGenerationView is required";
    public static final String REQUIRE_MAP_VALIDATION_INTERNALS_VIEW = "mapValidationInternalsView is required";

    public static final String REQUIRE_GAME_MANAGER = "gameManager is required";
    public static final String REQUIRE_CLI_HANDLER = "cliHandler is required";
    public static final String REQUIRE_WAY_FINDER = "wayFinder is required";
    public static final String REQUIRE_DYNAMIC_VIEW = "dynamicView is required";
    public static final String REQUIRE_SHARED_GAME_STATE = "sharedGameState is required";

    public static final String TYPE_GAME_STATE = "GameState";
    public static final String CONTEXT_SERVER_RESPONSE_VALIDATION = "server_response_validation";
    public static final String CONTEXT_STATE_CONVERSION = "state_conversion";
    public static final String CONTEXT_UPDATE_PROCESS = "update_process";

    public static final String ERROR_RECEIVED_MISSING_GAME_STATE_FROM_SERVER = "Received missing game state from server";
    public static final String ERROR_FAILED_CONVERT_SERVER_GAME_STATE = "Failed to convert server game state to client format";
    public static final String ERROR_UNEXPECTED_GAME_STATE_UPDATE_PREFIX = "Unexpected error during game state update: ";

    public static final String ERROR_FAILED_CHECK_FULL_MAP_AVAILABILITY_PREFIX = "Failed to check full map availability: ";
    public static final String ERROR_FAILED_POLL_GAME_STATE_PREFIX = "Failed to poll game state from server: ";

    public static final String ERROR_PLAYER_MUST_BE_REGISTERED_POLL_STATE = "Player must be registered before polling game state";
    public static final String ERROR_RECEIVED_MISSING_GAME_STATE_FROM_SERVER_RESPONSE = "Received game state missing from server response";
    public static final String ERROR_HTTP_ERROR_POLLING_GAME_STATE_PREFIX = "HTTP error during game state polling: ";
    public static final String ERROR_GAME_STATE_POLLING_INTERRUPTED = "Game state polling was interrupted";
    public static final String ERROR_NETWORK_CONNECTION_FAILED_POLLING_GAME_STATE = "Network connection failed during game state polling";

    public static final String ERROR_CANNOT_MAKE_MOVE_GAME_STATE_MISSING = "Cannot make move: game state is missing";
    public static final String ERROR_CANNOT_MAKE_MOVE_PLAYER_STATE_MISSING = "Cannot make move: current player state is missing";
    public static final String OP_MAKE_MOVE = "MAKE_MOVE";
    public static final String REASON_MISSING_PLAYER_STATE = "missing_player_state";

    public static final String AI_COMPONENT_WAY_FINDER = "WayFinder";
    public static final String AI_CONTEXT_FIND_NEXT = "findNext";
    public static final String ERROR_WAY_FINDER_MISSING_NEXT_MOVE = "WayFinder returned a missing next move";
    public static final String UNKNOWN_POSITION = "<unknown_position>";
    public static final String ERROR_AI_NO_FALLBACK = "AI could not determine a move and did not provide a fallback";
    public static final String AI_FALLBACK_MESSAGE_PREFIX = "AI could not compute a move (";
    public static final String AI_FALLBACK_MESSAGE_MIDDLE = "). Falling back to: ";

    public static final String ERROR_FAILED_SEND_MOVE_PREFIX = "Failed to send move to server: ";
    public static final String ERROR_UNEXPECTED_MOVE_MAKING_PREFIX = "Unexpected error during move making: ";

    public static final String ERROR_CANNOT_REGISTER_PLAYER_GAME_STATE_MISSING = "Cannot register player: game state is missing";
    public static final String ERROR_UNEXPECTED_PLAYER_REGISTRATION_PREFIX = "Unexpected error during player registration: ";

    public static final String ERROR_CANNOT_GET_PLAYER_STATUS_GAME_STATE_MISSING = "Cannot get player status: game state is missing";
    public static final String ERROR_CANNOT_GET_PLAYER_STATUS_PLAYERS_MISSING = "Cannot get player status: players list is missing";
    public static final String ERROR_PLAYER_ID_NOT_FOUND = "Player ID not found in game state";
    public static final String OP_GET_PLAYER_STATUS = "GET_PLAYER_STATUS";
    public static final String REASON_MISSING_PLAYERS = "missing_players";
    public static final String REASON_PLAYER_NOT_FOUND = "player_not_found";
    public static final String EXPECTED_PLAYER_PRESENT = "player_present";

    public static final String ERROR_FATAL_MAP_PROCESSING_PREFIX = "Fatal map processing error: ";
    public static final String ERROR_UNEXPECTED_GAME_LOOP_PREFIX = "Unexpected error in game loop: ";

    public static final String ERROR_PLAYER_MUST_BE_REGISTERED_CONVERT_STATE = "Player must be registered before converting game state";

    public static final String FETCH_GAME_ID_QUERY = "games?enableDummyCompetition=true";
    public static final String HTTP_METHOD_GET = "GET";
    public static final int HTTP_TIMEOUT_MILLIS = 5000;
    public static final int HTTP_STATUS_OK = 200;
    public static final String XML_TAG_GAME_ID_START = "<uniqueGameID>";
    public static final String XML_TAG_GAME_ID_END = "</uniqueGameID>";
    public static final String ERROR_FAILED_FETCH_GAME_ID = "Failed to fetch game ID";
    public static final String ERROR_FETCH_GAME_ID_PREFIX = "Error fetching game ID from server: ";
    public static final String ERROR_CANNOT_FIND_GAME_ID_IN_RESPONSE = "Could not find uniqueGameID in server response";
    public static final String ERROR_SERVER_RETURNED_EMPTY_GAME_ID = "Server returned empty game ID";

    public static final String ERROR_UNSUPPORTED_DIRECTION_PREFIX = "Unsupported direction: ";
    public static final String ERROR_SERVER_STATUS_REQUIRED = "Server status must be provided";
    public static final String ERROR_UNKNOWN_SERVER_STATUS_PREFIX = "Unknown server status: ";

    public static final String PLAYER_ID_KEY = "PLAYER_ID";

    public static final String ERROR_PLAYER_IDENTIFIER_MISSING = "Player identifier is missing";

    public static final String HALF_MAP_TITLE_OWN = "Own Half Map";
    public static final String ERROR_CANNOT_GENERATE_HALF_MAP_NO_PLAYER_ID = "Cannot generate half map: player ID is missing";
    public static final String ERROR_FAILED_GENERATE_OR_SEND_HALF_MAP_PREFIX = "Failed to generate or send half map: ";
    public static final String ERROR_UNABLE_TO_GENERATE_VALID_HALF_MAP_AFTER_PREFIX = "Unable to generate a valid half map after ";
    public static final String ERROR_UNABLE_TO_GENERATE_VALID_HALF_MAP_AFTER_SUFFIX = " attempts";
    public static final String MAP_VALIDATION_ATTEMPT_PREFIX = "Attempt ";
    public static final String MAP_VALIDATION_ATTEMPT_SEPARATOR = "/";
    public static final String MAP_VALIDATION_ATTEMPT_SUFFIX = ": ";
    public static final String ERROR_FAILED_SEND_HALF_MAP_PREFIX = "Failed to send half map to server: ";

    public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HTTP_HEADER_ACCEPT = "Accept";

    public static final String MEDIA_TYPE_APPLICATION_XML = "application/xml";

    public static final String PATH_GAMES = "/games";
    public static final String PATH_PLAYERS = "/players";
    public static final String PATH_HALFMAPS = "/halfmaps";
    public static final String PATH_MOVES = "/moves";
    public static final String PATH_STATES_PREFIX = "/states/";

    public static final String OP_PLAYER_REGISTRATION = "PLAYER_REGISTRATION";
    public static final String OP_SEND_HALF_MAP = "SEND_HALF_MAP";
    public static final String OP_SEND_MOVE = "SEND_MOVE";
    public static final String OP_POLL_GAME_STATE = "POLL_GAME_STATE";

    public static final String FALLBACK_OPERATION_UNKNOWN = "UNKNOWN";
    public static final String FALLBACK_SERVER_ERROR_NAME = "ServerError";

    public static final String SERVER_REJECTED_PREFIX = "Server rejected ";
    public static final String SERVER_REJECTED_SEPARATOR_1 = ": ";
    public static final String SERVER_REJECTED_SEPARATOR_2 = " - ";

    public static final long HTTP_STATUS_UNKNOWN = -1L;
    public static final int HTTP_STATUS_UNKNOWN_INT = -1;
    public static final int HTTP_STATUS_SERVER_REJECTED = 400;

    public static final String OP_FETCH_GAME_ID = "fetchGameId";

    public static Optional<String> optionalNonBlank(String value) {
        try {
            String trimmed = Objects.requireNonNull(value).trim();
            if (trimmed.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(trimmed);
        } catch (NullPointerException e) {
            return Optional.empty();
        }
    }

    public static String nonBlankOrUnknown(String value) {
        return optionalNonBlank(value).orElse(UNKNOWN);
    }

    public static <T> T defaultIfMissing(T value, T defaultValue) {
        try {
            return Objects.requireNonNull(value);
        } catch (NullPointerException e) {
            return defaultValue;
        }
    }

    public static String safeStringOrDefault(String value, String defaultValue) {
        try {
            return Objects.requireNonNull(value);
        } catch (NullPointerException e) {
            return defaultValue;
        }
    }

    public static <T> T requireNonNullOrThrow(T value, RuntimeException exceptionToThrow) {
        try {
            return Objects.requireNonNull(value);
        } catch (NullPointerException e) {
            throw exceptionToThrow;
        }
    }
}
