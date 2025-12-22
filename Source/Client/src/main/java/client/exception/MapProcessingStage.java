package client.exception;

public enum MapProcessingStage {
    SERVER_RESPONSE_VALIDATION("server_response_validation"),
    STATE_CONVERSION("state_conversion"),
    UPDATE_PROCESS("update_process"),
    CONVERSION("conversion"),
    NETWORK("network"),
    SERVER("server"),
    UNKNOWN("unknown");

    private final String code;

    MapProcessingStage(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
