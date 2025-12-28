package client.exception;

import java.util.Objects;
import java.util.Optional;

/**
 * Signals failures while processing or validating map data.
 *
 * <p>Includes optional context (map type, stage, expected/actual nodes, coordinate context)
 * to support user-friendly recovery messages.
 */
public class MapProcessingException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private final Optional<String> mapType;
    private final int expectedNodes;
    private final int actualNodes;
    private final Optional<String> processingStage;
    private final Optional<String> coordinateContext;

    public MapProcessingException(String message, String mapType, String processingStage) {
        super(buildDetailedMessage(message,
                Optional.ofNullable(mapType),
                Optional.ofNullable(processingStage),
                -1,
                -1,
                Optional.empty()));
        this.mapType = Optional.ofNullable(mapType);
        this.processingStage = Optional.ofNullable(processingStage);
        this.expectedNodes = -1;
        this.actualNodes = -1;
        this.coordinateContext = Optional.empty();
    }
    
    public MapProcessingException(String message, Throwable cause, String mapType, String processingStage, 
                                int expectedNodes, int actualNodes, String coordinateContext) {
        super(buildDetailedMessage(message,
            Optional.ofNullable(mapType),
            Optional.ofNullable(processingStage),
            expectedNodes,
            actualNodes,
            Optional.ofNullable(coordinateContext)), cause);
        this.mapType = Optional.ofNullable(mapType);
        this.processingStage = Optional.ofNullable(processingStage);
        this.expectedNodes = expectedNodes;
        this.actualNodes = actualNodes;
        this.coordinateContext = Optional.ofNullable(coordinateContext);
    }

    private static String buildDetailedMessage(
            String message,
            Optional<String> mapType,
            Optional<String> processingStage,
            int expectedNodes,
            int actualNodes,
            Optional<String> coordinateContext
    ) {
        StringBuilder sb = new StringBuilder(TextCnofig.PREFIX_MAP_PROCESSING_ERROR).append(message);
        Objects.requireNonNull(mapType, TextCnofig.REQUIRE_MAP_TYPE_IS_REQUIRED)
            .filter(value -> !value.isBlank())
            .ifPresent(value -> sb.append(TextCnofig.LABEL_BRACKET_OPEN).append(TextCnofig.LABEL_MAP_TYPE).append(value).append(TextCnofig.LABEL_BRACKET_CLOSE));
        Objects.requireNonNull(processingStage, TextCnofig.REQUIRE_PROCESSING_STAGE_IS_REQUIRED)
            .filter(value -> !value.isBlank())
            .ifPresent(value -> sb.append(TextCnofig.LABEL_BRACKET_OPEN).append(TextCnofig.LABEL_STAGE).append(value).append(TextCnofig.LABEL_BRACKET_CLOSE));
        if (expectedNodes > 0 && actualNodes >= 0) {
            sb.append(TextCnofig.LABEL_BRACKET_OPEN)
                    .append(TextCnofig.LABEL_EXPECTED_NODES).append(expectedNodes)
                    .append(", ").append(TextCnofig.LABEL_ACTUAL_NODES).append(actualNodes)
                    .append(TextCnofig.LABEL_BRACKET_CLOSE);
        }
        Objects.requireNonNull(coordinateContext, TextCnofig.REQUIRE_COORDINATE_CONTEXT_IS_REQUIRED)
            .filter(value -> !value.isBlank())
            .ifPresent(value -> sb.append(TextCnofig.LABEL_BRACKET_OPEN).append(TextCnofig.LABEL_COORDINATES).append(value).append(TextCnofig.LABEL_BRACKET_CLOSE));
        return sb.toString();
    }

    private boolean hasNodeCountInfo() {
        return expectedNodes > 0 && actualNodes >= 0;
    }

    public boolean isRecoverable() {
        // Consider recoverable if it's a node count issue (might be partial data)
        // or if it's during server conversion (might be temporary server issue)
        if (hasNodeCountInfo() && actualNodes < expectedNodes) {
            return true; // Partial data - might be recoverable
        }

        return processingStage
                .map(String::toLowerCase)
                .map(stage -> stage.contains("server") || stage.contains("network") || stage.contains("conversion"))
                .orElse(false);
    }

    public String getRecoveryMessage() {
        StringBuilder recovery = new StringBuilder();
        recovery.append(TextCnofig.MAP_RECOVERY_PREFIX).append(getMessage()).append("\n");

        mapType.filter(value -> !value.isBlank()).ifPresent(value -> recovery.append(TextCnofig.MAP_RECOVERY_MAP_TYPE_LINE).append(value).append("\n"));
        processingStage.filter(value -> !value.isBlank()).ifPresent(value -> recovery.append(TextCnofig.MAP_RECOVERY_STAGE_LINE).append(value).append("\n"));
        coordinateContext.filter(value -> !value.isBlank()).ifPresent(value -> recovery.append(TextCnofig.MAP_RECOVERY_COORDINATES_LINE).append(value).append("\n"));
        
        if (hasNodeCountInfo()) {
            recovery.append(TextCnofig.MAP_RECOVERY_NODES_PREFIX).append(expectedNodes)
                   .append(TextCnofig.MAP_RECOVERY_NODES_SEPARATOR).append(actualNodes).append("\n");
        }
        
        if (isRecoverable()) {
            recovery.append(TextCnofig.MAP_RECOVERY_ACTION_RECOVERABLE);
            recovery.append(TextCnofig.MAP_RECOVERY_HINT_DEBUG_TRACES_ROOT_CAUSE);
        } else {
            recovery.append(TextCnofig.MAP_RECOVERY_ACTION_NOT_RECOVERABLE);
            recovery.append(TextCnofig.MAP_RECOVERY_HINT_PROTOCOL_COMPAT);
            recovery.append(TextCnofig.MAP_RECOVERY_HINT_DEBUG_TRACES_STACK);
        }
        
        return recovery.toString();
    }
}