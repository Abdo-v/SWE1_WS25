package client.exception;

/**
 * Unchecked exception for AI algorithm decision-making failures.
 * 
 * This exception is thrown when the AI encounters an unrecoverable error
 * in its decision-making process. Since these represent programming logic
 * errors or invalid game states that should not occur in normal operation,
 * this is implemented as an unchecked exception (RuntimeException).
 * 
 * Use cases:
 * - AI pathfinding algorithm failures (no valid path found when one should exist)
 * - Invalid game state transitions that break AI assumptions
 * - Algorithm infinite loops or stack overflow conditions
 * - Corrupted AI decision trees or invalid strategy states
 * - Mathematical errors in AI calculations
 * 
 * @author Abdalrahman Mohammed
 */
public class AIDecisionException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final String aiComponent;
    private final String decisionContext;
    private final Object gameStateSnapshot;
    
    /**
     * Creates a new AIDecisionException with a message.
     * 
     * @param message the detail message explaining the AI decision failure
     */
    public AIDecisionException(String message) {
        super(message);
        this.aiComponent = null;
        this.decisionContext = null;
        this.gameStateSnapshot = null;
    }
    
    /**
     * Creates a new AIDecisionException with a message and cause.
     * 
     * @param message the detail message explaining the AI decision failure
     * @param cause the underlying cause of the AI failure
     */
    public AIDecisionException(String message, Throwable cause) {
        super(message, cause);
        this.aiComponent = null;
        this.decisionContext = null;
        this.gameStateSnapshot = null;
    }
    
    /**
     * Creates a new AIDecisionException with detailed AI context information.
     * 
     * @param message the detail message explaining the AI decision failure
     * @param aiComponent the AI component that failed (e.g., "PathFinder", "StrategyEngine")
     * @param decisionContext the context of the decision being made
     */
    public AIDecisionException(String message, String aiComponent, String decisionContext) {
        super(buildDetailedMessage(message, aiComponent, decisionContext));
        this.aiComponent = aiComponent;
        this.decisionContext = decisionContext;
        this.gameStateSnapshot = null;
    }
    
    /**
     * Creates a new AIDecisionException with detailed AI context and game state snapshot.
     * 
     * @param message the detail message explaining the AI decision failure
     * @param aiComponent the AI component that failed
     * @param decisionContext the context of the decision being made
     * @param gameStateSnapshot a snapshot of the game state for debugging
     */
    public AIDecisionException(String message, String aiComponent, String decisionContext, Object gameStateSnapshot) {
        super(buildDetailedMessage(message, aiComponent, decisionContext));
        this.aiComponent = aiComponent;
        this.decisionContext = decisionContext;
        this.gameStateSnapshot = gameStateSnapshot;
    }
    
    /**
     * Creates a new AIDecisionException with detailed context and cause.
     * 
     * @param message the detail message explaining the AI decision failure
     * @param cause the underlying cause of the AI failure
     * @param aiComponent the AI component that failed
     * @param decisionContext the context of the decision being made
     * @param gameStateSnapshot a snapshot of the game state for debugging
     */
    public AIDecisionException(String message, Throwable cause, String aiComponent, String decisionContext, Object gameStateSnapshot) {
        super(buildDetailedMessage(message, aiComponent, decisionContext), cause);
        this.aiComponent = aiComponent;
        this.decisionContext = decisionContext;
        this.gameStateSnapshot = gameStateSnapshot;
    }
    
    /**
     * Builds a detailed error message with AI context information.
     */
    private static String buildDetailedMessage(String message, String aiComponent, String decisionContext) {
        StringBuilder sb = new StringBuilder("AI Decision Failure: ").append(message);
        if (aiComponent != null) {
            sb.append(" [Component: ").append(aiComponent).append("]");
        }
        if (decisionContext != null) {
            sb.append(" [Context: ").append(decisionContext).append("]");
        }
        return sb.toString();
    }
    
    // Getters for additional context information
    public String getAiComponent() { return aiComponent; }
    public String getDecisionContext() { return decisionContext; }
    public Object getGameStateSnapshot() { return gameStateSnapshot; }
    
    /**
     * Creates a formatted debug report for developers.
     * 
     * @return a detailed debug report of the AI failure
     */
    public String getDebugReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== AI DECISION EXCEPTION DEBUG REPORT ===\n");
        report.append("Message: ").append(getMessage()).append("\n");
        report.append("AI Component: ").append(aiComponent != null ? aiComponent : "Unknown").append("\n");
        report.append("Decision Context: ").append(decisionContext != null ? decisionContext : "Unknown").append("\n");
        report.append("Has Game State Snapshot: ").append(gameStateSnapshot != null ? "Yes" : "No").append("\n");
        if (getCause() != null) {
            report.append("Underlying Cause: ").append(getCause().getClass().getSimpleName())
                  .append(" - ").append(getCause().getMessage()).append("\n");
        }
        report.append("Stack Trace: Available via printStackTrace()").append("\n");
        report.append("=== END DEBUG REPORT ===");
        return report.toString();
    }
}