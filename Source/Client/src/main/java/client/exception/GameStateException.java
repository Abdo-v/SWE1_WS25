package client.exception;

/**
 * Unchecked exception for game state consistency violations.
 * 
 * This exception is thrown when the game state becomes inconsistent
 * or when operations are attempted on invalid game states.
 */
public class GameStateException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final java.util.Optional<String> gameStateId;
    private final java.util.Optional<String> operation;
    private final java.util.Optional<String> currentState;
    private final java.util.Optional<String> expectedState;
    
    /**
     * Creates a new GameStateException with a message.
     * 
     * @param message the detail message explaining the game state failure
     */
    public GameStateException(String message) {
        super(message);
        this.gameStateId = java.util.Optional.empty();
        this.operation = java.util.Optional.empty();
        this.currentState = java.util.Optional.empty();
        this.expectedState = java.util.Optional.empty();
    }
    
    /**
     * Creates a new GameStateException with a message and cause.
     * 
     * @param message the detail message explaining the game state failure
     * @param cause the underlying cause of the game state failure
     */
    public GameStateException(String message, Throwable cause) {
        super(message, cause);
        this.gameStateId = java.util.Optional.empty();
        this.operation = java.util.Optional.empty();
        this.currentState = java.util.Optional.empty();
        this.expectedState = java.util.Optional.empty();
    }
    
    /**
     * Creates a new GameStateException with detailed game state context.
     * 
     * @param message the detail message explaining the game state failure
     * @param gameStateId the ID of the game state that failed
     * @param operation the operation that was being performed
     * @param currentState the current state when the error occurred
     */
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
    
    /**
     * Creates a new GameStateException with detailed context including expected state.
     * 
     * @param message the detail message explaining the game state failure
     * @param gameStateId the ID of the game state that failed
     * @param operation the operation that was being performed
     * @param currentState the current state when the error occurred
     * @param expectedState the expected state for the operation
     */
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
    
    /**
     * Creates a new GameStateException with detailed context and cause.
     * 
     * @param message the detail message explaining the game state failure
     * @param cause the underlying cause of the game state failure
     * @param gameStateId the ID of the game state that failed
     * @param operation the operation that was being performed
     * @param currentState the current state when the error occurred
     * @param expectedState the expected state for the operation
     */
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
    
    /**
     * Builds a detailed error message with game state context information.
     */
    private static String buildDetailedMessage(
            String message,
            java.util.Optional<String> gameStateId,
            java.util.Optional<String> operation,
            java.util.Optional<String> currentState,
            java.util.Optional<String> expectedState
    ) {
        StringBuilder sb = new StringBuilder("Game State Error: ").append(message);
        java.util.Objects.requireNonNull(gameStateId, "gameStateId is required")
            .filter(id -> !id.isBlank())
            .ifPresent(id -> sb.append(" [Game: ").append(id).append("]"));
        java.util.Objects.requireNonNull(operation, "operation is required")
            .filter(op -> !op.isBlank())
            .ifPresent(op -> sb.append(" [Operation: ").append(op).append("]"));
        java.util.Objects.requireNonNull(currentState, "currentState is required")
            .filter(st -> !st.isBlank())
            .ifPresent(st -> sb.append(" [Current State: ").append(st).append("]"));
        java.util.Objects.requireNonNull(expectedState, "expectedState is required")
            .filter(st -> !st.isBlank())
            .ifPresent(st -> sb.append(" [Expected State: ").append(st).append("]"));
        return sb.toString();
    }
    
    // Getters for additional context information
    public java.util.Optional<String> getGameStateId() { return gameStateId; }
    public java.util.Optional<String> getOperation() { return operation; }
    public java.util.Optional<String> getCurrentState() { return currentState; }
    public java.util.Optional<String> getExpectedState() { return expectedState; }
    
    /**
     * Determines if this exception includes information about the expected state.
     * 
     * @return true if expected state information is available
     */
    private boolean hasExpectedState() {
        return expectedState.filter(state -> !state.trim().isEmpty()).isPresent();
    }
    
    /**
     * Creates a formatted debug report for developers.
     * 
     * @return a detailed debug report of the game state failure
     */
    public String getDebugReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== GAME STATE EXCEPTION DEBUG REPORT ===\n");
        report.append("Message: ").append(getMessage()).append("\n");
        report.append("Game State ID: ").append(gameStateId.orElse("Unknown")).append("\n");
        report.append("Failed Operation: ").append(operation.orElse("Unknown")).append("\n");
        report.append("Current State: ").append(currentState.orElse("Unknown")).append("\n");
        report.append("Expected State: ").append(expectedState.orElse("Not specified")).append("\n");
        
        java.util.Optional.ofNullable(getCause()).ifPresent(cause -> report
            .append("Underlying Cause: ").append(cause.getClass().getSimpleName())
            .append(" - ").append(cause.getMessage()).append("\n"));
        
        report.append("Timestamp: ").append(java.time.LocalDateTime.now()).append("\n");
        report.append("Stack Trace: Available via printStackTrace()").append("\n");
        report.append("=== END DEBUG REPORT ===");
        return report.toString();
    }
    
    /**
     * Creates a user-friendly error message for display purposes.
     * 
     * @return a formatted error message suitable for user display
     */
    public String getUserMessage() {
        StringBuilder userMsg = new StringBuilder();
        userMsg.append("🎮 Game State Error: ").append(getMessage()).append("\n");

        operation.filter(op -> !op.isBlank()).ifPresent(op -> userMsg.append("📋 During operation: ").append(op).append("\n"));
        
        if (hasExpectedState()) {
            userMsg.append("⚠️  Expected state: ").append(expectedState.orElse(""))
                   .append(", but found: ").append(currentState.orElse("unknown")).append("\n");
        }
        
        userMsg.append("💡 This indicates an internal error. Please try restarting the game.");
        
        return userMsg.toString();
    }
}