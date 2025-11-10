package client.exception;

/**
 * Unchecked exception for game state consistency violations.
 * 
 * This exception is thrown when the game state becomes inconsistent
 * or when operations are attempted on invalid game states. These
 * represent programming logic errors rather than expected conditions,
 * making this an unchecked exception.
 * 
 * Use cases:
 * - Inconsistent player state transitions
 * - Operations on null or uninitialized game states
 * - Observer pattern violations
 * - Invalid game state modifications
 * - Corrupted game state data structures
 * 
 * @author Abdalrahman Mohammed
 */
public class GameStateException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final String gameStateId;
    private final String operation;
    private final String currentState;
    private final String expectedState;
    
    /**
     * Creates a new GameStateException with a message.
     * 
     * @param message the detail message explaining the game state failure
     */
    public GameStateException(String message) {
        super(message);
        this.gameStateId = null;
        this.operation = null;
        this.currentState = null;
        this.expectedState = null;
    }
    
    /**
     * Creates a new GameStateException with a message and cause.
     * 
     * @param message the detail message explaining the game state failure
     * @param cause the underlying cause of the game state failure
     */
    public GameStateException(String message, Throwable cause) {
        super(message, cause);
        this.gameStateId = null;
        this.operation = null;
        this.currentState = null;
        this.expectedState = null;
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
        super(buildDetailedMessage(message, gameStateId, operation, currentState, null));
        this.gameStateId = gameStateId;
        this.operation = operation;
        this.currentState = currentState;
        this.expectedState = null;
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
        super(buildDetailedMessage(message, gameStateId, operation, currentState, expectedState));
        this.gameStateId = gameStateId;
        this.operation = operation;
        this.currentState = currentState;
        this.expectedState = expectedState;
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
        super(buildDetailedMessage(message, gameStateId, operation, currentState, expectedState), cause);
        this.gameStateId = gameStateId;
        this.operation = operation;
        this.currentState = currentState;
        this.expectedState = expectedState;
    }
    
    /**
     * Builds a detailed error message with game state context information.
     */
    private static String buildDetailedMessage(String message, String gameStateId, String operation, String currentState, String expectedState) {
        StringBuilder sb = new StringBuilder("Game State Error: ").append(message);
        if (gameStateId != null) {
            sb.append(" [Game: ").append(gameStateId).append("]");
        }
        if (operation != null) {
            sb.append(" [Operation: ").append(operation).append("]");
        }
        if (currentState != null) {
            sb.append(" [Current State: ").append(currentState).append("]");
        }
        if (expectedState != null) {
            sb.append(" [Expected State: ").append(expectedState).append("]");
        }
        return sb.toString();
    }
    
    // Getters for additional context information
    public String getGameStateId() { return gameStateId; }
    public String getOperation() { return operation; }
    public String getCurrentState() { return currentState; }
    public String getExpectedState() { return expectedState; }
    
    /**
     * Determines if this exception includes information about the expected state.
     * 
     * @return true if expected state information is available
     */
    public boolean hasExpectedState() {
        return expectedState != null && !expectedState.trim().isEmpty();
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
        report.append("Game State ID: ").append(gameStateId != null ? gameStateId : "Unknown").append("\n");
        report.append("Failed Operation: ").append(operation != null ? operation : "Unknown").append("\n");
        report.append("Current State: ").append(currentState != null ? currentState : "Unknown").append("\n");
        report.append("Expected State: ").append(expectedState != null ? expectedState : "Not specified").append("\n");
        
        if (getCause() != null) {
            report.append("Underlying Cause: ").append(getCause().getClass().getSimpleName())
                  .append(" - ").append(getCause().getMessage()).append("\n");
        }
        
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
        
        if (operation != null) {
            userMsg.append("📋 During operation: ").append(operation).append("\n");
        }
        
        if (hasExpectedState()) {
            userMsg.append("⚠️  Expected state: ").append(expectedState)
                   .append(", but found: ").append(currentState != null ? currentState : "unknown").append("\n");
        }
        
        userMsg.append("💡 This indicates an internal error. Please try restarting the game.");
        
        return userMsg.toString();
    }
}