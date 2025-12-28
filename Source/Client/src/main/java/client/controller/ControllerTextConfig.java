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

    public static final String PLAYER_ID_KEY = "PLAYER_ID";

    public static final String ERROR_PLAYER_IDENTIFIER_MISSING = "Player identifier is missing";

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
}
