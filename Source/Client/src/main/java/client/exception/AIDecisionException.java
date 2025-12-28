package client.exception;

import java.util.Optional;

/**
 * Signals that the AI could not produce a decision for the current situation.
 *
 * <p>Optionally carries lightweight context (component, decision context, and a state snapshot)
 * to make debugging easier without forcing callers to log internals everywhere.
 */
public class AIDecisionException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final Optional<String> aiComponent;
    private final Optional<String> decisionContext;
    private final Optional<Object> gameStateSnapshot;
    
    public AIDecisionException(String message) {
        super(message);
        this.aiComponent = Optional.empty();
        this.decisionContext = Optional.empty();
        this.gameStateSnapshot = Optional.empty();
    }
    
    public AIDecisionException(String message, Throwable cause) {
        super(message, cause);
        this.aiComponent = Optional.empty();
        this.decisionContext = Optional.empty();
        this.gameStateSnapshot = Optional.empty();
    }
    
    public AIDecisionException(String message, String aiComponent, String decisionContext) {
        super(buildDetailedMessage(message, aiComponent, decisionContext));
        this.aiComponent = Optional.ofNullable(aiComponent);
        this.decisionContext = Optional.ofNullable(decisionContext);
        this.gameStateSnapshot = Optional.empty();
    }
    
    public AIDecisionException(String message, String aiComponent, String decisionContext, Object gameStateSnapshot) {
        super(buildDetailedMessage(message, aiComponent, decisionContext));
        this.aiComponent = Optional.ofNullable(aiComponent);
        this.decisionContext = Optional.ofNullable(decisionContext);
        this.gameStateSnapshot = Optional.ofNullable(gameStateSnapshot);
    }
    
    public AIDecisionException(String message, Throwable cause, String aiComponent, String decisionContext, Object gameStateSnapshot) {
        super(buildDetailedMessage(message, aiComponent, decisionContext), cause);
        this.aiComponent = Optional.ofNullable(aiComponent);
        this.decisionContext = Optional.ofNullable(decisionContext);
        this.gameStateSnapshot = Optional.ofNullable(gameStateSnapshot);
    }
    
    private static String buildDetailedMessage(String message, String aiComponent, String decisionContext) {
        StringBuilder sb = new StringBuilder(TextCnofig.PREFIX_AI_DECISION_FAILURE).append(message);
        Optional.ofNullable(aiComponent).ifPresent(component -> sb.append(TextCnofig.LABEL_BRACKET_OPEN)
            .append(TextCnofig.LABEL_COMPONENT).append(component).append(TextCnofig.LABEL_BRACKET_CLOSE));
        Optional.ofNullable(decisionContext).ifPresent(context -> sb.append(TextCnofig.LABEL_BRACKET_OPEN)
            .append(TextCnofig.LABEL_CONTEXT).append(context).append(TextCnofig.LABEL_BRACKET_CLOSE));
        return sb.toString();
    }
    
    public Optional<String> getAiComponent() { return aiComponent; }
    public Optional<String> getDecisionContext() { return decisionContext; }
    public Optional<Object> getGameStateSnapshot() { return gameStateSnapshot; }

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