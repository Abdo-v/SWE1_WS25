package client.exception;

/**
 * Signals that the current client-side game state cannot support an operation.
 *
 * <p>This is typically used for invariant/consistency violations, not for network failures.
 * Optional context (game id, operation, current/expected state) is kept for diagnostics.
 */
public class GameStateException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    private static final String UNKNOWN = TextCnofig.UNKNOWN_TEXT;
    
    private final java.util.Optional<String> gameStateId;
    private final java.util.Optional<String> operation;
    private final java.util.Optional<String> currentState;
    private final java.util.Optional<String> expectedState;
    
    public GameStateException(String message) {
        super(message);
        this.gameStateId = java.util.Optional.empty();
        this.operation = java.util.Optional.empty();
        this.currentState = java.util.Optional.empty();
        this.expectedState = java.util.Optional.empty();
    }
    
    public GameStateException(String message, Throwable cause) {
        super(message, cause);
        this.gameStateId = java.util.Optional.empty();
        this.operation = java.util.Optional.empty();
        this.currentState = java.util.Optional.empty();
        this.expectedState = java.util.Optional.empty();
    }
    
    public GameStateException(String message, String gameStateId, String operation, String currentState) {
        super(buildDetailedMessage(message,
            java.util.Optional.ofNullable(gameStateId),
            java.util.Optional.ofNullable(operation),
            java.util.Optional.ofNullable(currentState),
            java.util.Optional.empty()));
        this.gameStateId = java.util.Optional.ofNullable(gameStateId);
        this.operation = java.util.Optional.ofNullable(operation);
        this.currentState = java.util.Optional.ofNullable(currentState);
        this.expectedState = java.util.Optional.empty();
    }
    
    public GameStateException(String message, String gameStateId, String operation, String currentState, String expectedState) {
        super(buildDetailedMessage(message,
            java.util.Optional.ofNullable(gameStateId),
            java.util.Optional.ofNullable(operation),
            java.util.Optional.ofNullable(currentState),
            java.util.Optional.ofNullable(expectedState)));
        this.gameStateId = java.util.Optional.ofNullable(gameStateId);
        this.operation = java.util.Optional.ofNullable(operation);
        this.currentState = java.util.Optional.ofNullable(currentState);
        this.expectedState = java.util.Optional.ofNullable(expectedState);
    }
    
    public GameStateException(String message, Throwable cause, String gameStateId, String operation, String currentState, String expectedState) {
        super(buildDetailedMessage(message,
            java.util.Optional.ofNullable(gameStateId),
            java.util.Optional.ofNullable(operation),
            java.util.Optional.ofNullable(currentState),
            java.util.Optional.ofNullable(expectedState)), cause);
        this.gameStateId = java.util.Optional.ofNullable(gameStateId);
        this.operation = java.util.Optional.ofNullable(operation);
        this.currentState = java.util.Optional.ofNullable(currentState);
        this.expectedState = java.util.Optional.ofNullable(expectedState);
    }

    public GameStateException(String message, String gameStateId, Operation operation, FailureReason currentState) {
        this(message,
            gameStateId,
            java.util.Optional.ofNullable(operation).map(Operation::code).orElse(""),
            java.util.Optional.ofNullable(currentState).map(FailureReason::code).orElse(""));
    }

    public GameStateException(String message, String gameStateId, Operation operation, FailureReason currentState, FailureReason expectedState) {
        this(message,
            gameStateId,
            java.util.Optional.ofNullable(operation).map(Operation::code).orElse(""),
            java.util.Optional.ofNullable(currentState).map(FailureReason::code).orElse(""),
            java.util.Optional.ofNullable(expectedState).map(FailureReason::code).orElse(""));
    }

    public GameStateException(String message, Throwable cause, String gameStateId, Operation operation, FailureReason currentState, FailureReason expectedState) {
        this(message,
            cause,
            gameStateId,
            java.util.Optional.ofNullable(operation).map(Operation::code).orElse(""),
            java.util.Optional.ofNullable(currentState).map(FailureReason::code).orElse(""),
            java.util.Optional.ofNullable(expectedState).map(FailureReason::code).orElse(""));
    }
    
    private static String buildDetailedMessage(
            String message,
            java.util.Optional<String> gameStateId,
            java.util.Optional<String> operation,
            java.util.Optional<String> currentState,
            java.util.Optional<String> expectedState
    ) {
        StringBuilder sb = new StringBuilder(TextCnofig.PREFIX_GAME_STATE_ERROR).append(message);
        java.util.Objects.requireNonNull(gameStateId, TextCnofig.REQUIRE_GAME_STATE_ID_IS_REQUIRED)
            .filter(id -> !id.isBlank())
            .ifPresent(id -> sb.append(TextCnofig.LABEL_BRACKET_OPEN).append(TextCnofig.LABEL_GAME).append(id).append(TextCnofig.LABEL_BRACKET_CLOSE));
        java.util.Objects.requireNonNull(operation, TextCnofig.REQUIRE_OPERATION_IS_REQUIRED)
            .filter(op -> !op.isBlank())
            .ifPresent(op -> sb.append(TextCnofig.LABEL_BRACKET_OPEN).append(TextCnofig.LABEL_OPERATION).append(op).append(TextCnofig.LABEL_BRACKET_CLOSE));
        java.util.Objects.requireNonNull(currentState, TextCnofig.REQUIRE_CURRENT_STATE_IS_REQUIRED)
            .filter(st -> !st.isBlank())
            .ifPresent(st -> sb.append(TextCnofig.LABEL_BRACKET_OPEN).append("Current State: ").append(st).append(TextCnofig.LABEL_BRACKET_CLOSE));
        java.util.Objects.requireNonNull(expectedState, TextCnofig.REQUIRE_EXPECTED_STATE_IS_REQUIRED)
            .filter(st -> !st.isBlank())
            .ifPresent(st -> sb.append(TextCnofig.LABEL_BRACKET_OPEN).append("Expected State: ").append(st).append(TextCnofig.LABEL_BRACKET_CLOSE));
        return sb.toString();
    }
    
    public java.util.Optional<String> getGameStateId() { return gameStateId; }
    public java.util.Optional<String> getOperation() { return operation; }
    public java.util.Optional<String> getCurrentState() { return currentState; }
    public java.util.Optional<String> getExpectedState() { return expectedState; }

    private boolean hasExpectedState() {
        return expectedState.filter(state -> !state.trim().isEmpty()).isPresent();
    }

    public String getDebugReport() {
        StringBuilder report = new StringBuilder();
        report.append(TextCnofig.DEBUG_REPORT_GAME_STATE_HEADER);
        report.append(TextCnofig.DEBUG_LABEL_MESSAGE).append(getMessage()).append("\n");
        report.append(TextCnofig.DEBUG_LABEL_GAME_STATE_ID).append(gameStateId.orElse(UNKNOWN)).append("\n");
        report.append(TextCnofig.DEBUG_LABEL_FAILED_OPERATION).append(operation.orElse(UNKNOWN)).append("\n");
        report.append(TextCnofig.DEBUG_LABEL_CURRENT_STATE).append(currentState.orElse(UNKNOWN)).append("\n");
        report.append(TextCnofig.DEBUG_LABEL_EXPECTED_STATE).append(expectedState.orElse(TextCnofig.NOT_SPECIFIED_TEXT)).append("\n");
        
        java.util.Optional.ofNullable(getCause()).ifPresent(cause -> report
            .append(TextCnofig.DEBUG_LABEL_UNDERLYING_CAUSE).append(cause.getClass().getSimpleName())
            .append(TextCnofig.DEBUG_CAUSE_SEPARATOR).append(cause.getMessage()).append("\n"));
        
        report.append(TextCnofig.DEBUG_LABEL_TIMESTAMP).append(java.time.LocalDateTime.now()).append("\n");
        report.append(TextCnofig.DEBUG_LABEL_STACK_TRACE_AVAILABLE).append("\n");
        report.append(TextCnofig.DEBUG_REPORT_END);
        return report.toString();
    }

    public String getUserMessage() {
        StringBuilder userMsg = new StringBuilder();
        userMsg.append(TextCnofig.USER_GAME_STATE_PREFIX).append(getMessage()).append("\n");

        operation.filter(op -> !op.isBlank()).ifPresent(op -> userMsg.append(TextCnofig.USER_DURING_OPERATION_PREFIX).append(op).append("\n"));
        
        if (hasExpectedState()) {
             userMsg.append(TextCnofig.USER_EXPECTED_STATE_PREFIX).append(expectedState.orElse(""))
                 .append(TextCnofig.USER_EXPECTED_STATE_SEPARATOR).append(currentState.orElse(TextCnofig.UNKNOWN_LOWER_TEXT)).append("\n");
        }
        
         userMsg.append(TextCnofig.USER_INTERNAL_ERROR_HINT);
        
        return userMsg.toString();
    }
}