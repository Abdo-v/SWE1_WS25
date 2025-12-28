package client.exception;

import java.util.Optional;

/**
 * Unchecked exception for AI algorithm decision-making failures.
 *
 * <p>This exception indicates that the AI could not complete a decision step
 * (e.g., selecting a move or computing a path) given the current internal state
 * and available game information.
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
        StringBuilder sb = new StringBuilder(TextCnofig.PREFIX_AI_DECISION_FAILURE).append(message);
        Optional.ofNullable(aiComponent).ifPresent(component -> sb.append(TextCnofig.LABEL_BRACKET_OPEN)
            .append(TextCnofig.LABEL_COMPONENT).append(component).append(TextCnofig.LABEL_BRACKET_CLOSE));
        Optional.ofNullable(decisionContext).ifPresent(context -> sb.append(TextCnofig.LABEL_BRACKET_OPEN)
            .append(TextCnofig.LABEL_CONTEXT).append(context).append(TextCnofig.LABEL_BRACKET_CLOSE));
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
        report.append(TextCnofig.DEBUG_REPORT_AI_HEADER);
        report.append(TextCnofig.DEBUG_LABEL_MESSAGE).append(getMessage()).append("\n");
        report.append(TextCnofig.DEBUG_LABEL_AI_COMPONENT).append(aiComponent.orElse(TextCnofig.UNKNOWN_TEXT)).append("\n");
        report.append(TextCnofig.DEBUG_LABEL_DECISION_CONTEXT).append(decisionContext.orElse(TextCnofig.UNKNOWN_TEXT)).append("\n");
        report.append(TextCnofig.DEBUG_LABEL_HAS_GAME_STATE_SNAPSHOT)
            .append(gameStateSnapshot.isPresent() ? TextCnofig.DEBUG_YES : TextCnofig.DEBUG_NO)
            .append("\n");
        Optional.ofNullable(getCause()).ifPresent(cause -> report
            .append(TextCnofig.DEBUG_LABEL_UNDERLYING_CAUSE).append(cause.getClass().getSimpleName())
            .append(TextCnofig.DEBUG_CAUSE_SEPARATOR).append(cause.getMessage()).append("\n"));
        report.append(TextCnofig.DEBUG_LABEL_STACK_TRACE_AVAILABLE).append("\n");
        report.append(TextCnofig.DEBUG_REPORT_END);
        return report.toString();
    }
}