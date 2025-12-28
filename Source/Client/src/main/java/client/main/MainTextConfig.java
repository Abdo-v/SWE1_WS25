package client.main;

import java.util.List;

final class MainTextConfig {

    private MainTextConfig() {
    }

    static final String ARG_GAME_MODE = "gameMode";
    static final String ARG_SERVER_BASE_URL = "serverBaseUrl";
    static final String ARG_GAME_ID = "gameId";
    static final String ARG_OPTIONS = "[options...]";

    static final String REQUIRE_GAME_MODE = "gameMode is required";
    static final String REQUIRE_SERVER_BASE_URL = "serverBaseUrl is required";
    static final String REQUIRE_GAME_ID = "gameId is required";

    static final String SUFFIX_IS_REQUIRED = " is required";

    static final String FIELD_ARGUMENTS = "arguments";
    static final String DETAIL_MISSING = "missing";
    static final String DETAIL_COUNT_PREFIX = "count=";

    static final String ERROR_ARGS_MISSING_EXPECTED_MODE_URL = "Arguments are missing. Expected: <gameMode> <serverBaseUrl>";
    static final String ERROR_ARGS_INSUFFICIENT_EXPECTED_MODE_URL = "Insufficient arguments provided. Expected: <gameMode> <serverBaseUrl>";

    static final String ERROR_GAME_ID_REQUIRED_PREFIX = "Game ID required for ";
    static final String ERROR_GAME_ID_REQUIRED_SUFFIX = " mode. Expected: <gameMode> <serverBaseUrl> <gameId>";

    static final String ERROR_GAME_MODE_REQUIRED = "Game mode is required";
    static final String ERROR_GAME_MODE_INVALID = "Invalid game mode provided";

    static final String ERROR_GAME_ID_MUST_BE_PRESENT_PREFIX = "gameId must be present for mode ";

    static final List<String> VALID_GAME_MODES = List.of("TR", "TRR", "ATTR");

    static String requiredArgumentMessage(String argName) {
        return argName + SUFFIX_IS_REQUIRED;
    }
}
