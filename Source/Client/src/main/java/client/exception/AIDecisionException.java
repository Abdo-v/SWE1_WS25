package client.exception;

import java.util.Optional;

/**
 * Unchecked exception for AI algorithm decision-making failures.
 * 
 * This exception is thrown when the AI encounters an unrecoverable error
 * in its decision-making process.
 * @author Abdalrahman Mohammed
 */
public class AIDecisionException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final Optional<String> aiComponent;
    private final Optional<String> decisionContext;
    private final Optional<Object> gameStateSnapshot;
    
    /**
     * Creates a new AIDecisionException with a message.
     * 
     * @param message the detail message explaining the AI decision failure
     */
    public AIDecisionException(String message) {
        super(message);
        this.aiComponent = Optional.empty();
        this.decisionContext = Optional.empty();
        this.gameStateSnapshot = Optional.empty();
    }
    
    /**
     * Creates a new AIDecisionException with a message and cause.
     * 
     * @param message the detail message explaining the AI decision failure
     * @param cause the underlying cause of the AI failure
     */
    public AIDecisionException(String message, Throwable cause) {
        super(message, cause);
        this.aiComponent = Optional.empty();
        this.decisionContext = Optional.empty();
        this.gameStateSnapshot = Optional.empty();
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
        this.aiComponent = Optional.ofNullable(aiComponent);
        this.decisionContext = Optional.ofNullable(decisionContext);
        this.gameStateSnapshot = Optional.empty();
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
        this.aiComponent = Optional.ofNullable(aiComponent);
        this.decisionContext = Optional.ofNullable(decisionContext);
        this.gameStateSnapshot = Optional.ofNullable(gameStateSnapshot);
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
        this.aiComponent = Optional.ofNullable(aiComponent);
        this.decisionContext = Optional.ofNullable(decisionContext);
        this.gameStateSnapshot = Optional.ofNullable(gameStateSnapshot);
    }
    
    /**
     * Builds a detailed error message with AI context information.
     */
    private static String buildDetailedMessage(String message, String aiComponent, String decisionContext) {
        StringBuilder sb = new StringBuilder("AI Decision Failure: ").append(message);
        Optional.ofNullable(aiComponent).ifPresent(component -> sb.append(" [Component: ").append(component).append("]"));
        Optional.ofNullable(decisionContext).ifPresent(context -> sb.append(" [Context: ").append(context).append("]"));
        return sb.toString();
    }
    
    // Getters for additional context information
    public Optional<String> getAiComponent() { return aiComponent; }
    public Optional<String> getDecisionContext() { return decisionContext; }
    public Optional<Object> getGameStateSnapshot() { return gameStateSnapshot; }
    
    /**
     * Creates a formatted debug report for developers.
     * 
     * @return a detailed debug report of the AI failure
     */
    public String getDebugReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== AI DECISION EXCEPTION DEBUG REPORT ===\n");
        report.append("Message: ").append(getMessage()).append("\n");
        report.append("AI Component: ").append(aiComponent.orElse("Unknown")).append("\n");
        report.append("Decision Context: ").append(decisionContext.orElse("Unknown")).append("\n");
        report.append("Has Game State Snapshot: ").append(gameStateSnapshot.isPresent() ? "Yes" : "No").append("\n");
        Optional.ofNullable(getCause()).ifPresent(cause -> report
                .append("Underlying Cause: ").append(cause.getClass().getSimpleName())
                .append(" - ").append(cause.getMessage()).append("\n"));
        report.append("Stack Trace: Available via printStackTrace()").append("\n");
        report.append("=== END DEBUG REPORT ===");
        return report.toString();
    }
}