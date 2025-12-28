package client.exception;

import java.util.Objects;
import java.util.Locale;

/**
 * Coarse-grained failure categories used for diagnostics and error reporting.
 *
 * <p>Values are mapped from a stable string {@link #code()} to keep logs and messages
 * consistent across layers.
 */
public enum FailureReason {
    UNINITIALIZED("uninitialized"),
    INVALID_STATE("invalid_state"),
    NULL_STATE("null_state"),
    NO_PLAYER_ID("no_player_id"),
    PLAYER_NOT_FOUND("player_not_found"),
    PLAYER_PRESENT("player_present"),
    MAP_ERROR("map_error"),
    UNEXPECTED_ERROR("unexpected_error"),
    ERROR("error"),
    UNKNOWN("unknown"),
    NO_NETWORK("no_network"),
    SERVER_RESPONSE_VALIDATION("server_response_validation"),
    STATE_CONVERSION("state_conversion"),
    UPDATE_PROCESS("update_process"),
    SERVER("server"),
    NETWORK("network"),
    CONVERSION("conversion");

    private final String code;

    FailureReason(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static FailureReason fromCode(String code) {
        String normalized = Objects.requireNonNullElse(code, "").trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return UNKNOWN;
        }
        for (FailureReason reason : values()) {
            if (reason.code.equals(normalized)) {
                return reason;
            }
        }
        return UNKNOWN;
    }
}
