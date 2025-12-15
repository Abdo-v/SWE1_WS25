package client.exception;

import java.util.Locale;

public enum Operation {
    PLAYER_REGISTRATION("PLAYER_REGISTRATION"),
    SEND_HALF_MAP("SEND_HALF_MAP"),
    GENERATE_HALF_MAP("GENERATE_HALF_MAP"),
    SEND_MOVE("SEND_MOVE"),
    POLL_GAME_STATE("POLL_GAME_STATE"),
    FULL_MAP_CHECK("FULL_MAP_CHECK"),
    POLL_PLAYER_STATUS("POLL_PLAYER_STATUS"),
    CHECK_SERVER_MAP_EMPTY("CHECK_SERVER_MAP_EMPTY"),
    GET_PLAYER_STATUS("GET_PLAYER_STATUS"),
    MAKE_MOVE("MAKE_MOVE"),
    GAME_LOOP("GAME_LOOP"),
    FETCH_GAME_ID("fetchGameId"),
    UNKNOWN("UNKNOWN");

    private final String code;

    Operation(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static Operation fromCode(String code) {
        if (code == null) {
            return UNKNOWN;
        }
        String normalized = code.trim();
        for (Operation op : values()) {
            if (op.code.equals(normalized)) {
                return op;
            }
        }

        // fallback: try case-insensitive match for typical ALL_CAPS codes
        String upper = normalized.toUpperCase(Locale.ROOT);
        for (Operation op : values()) {
            if (op.code.toUpperCase(Locale.ROOT).equals(upper)) {
                return op;
            }
        }

        return UNKNOWN;
    }
}
