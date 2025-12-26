package client.exception;

import java.util.Objects;
import java.util.Optional;

/**
 * Checked exception for map data processing failures.
 * 
 * This exception is thrown when map data cannot be properly processed,
 * converted, or validated.
 */
public class MapProcessingException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private final Optional<String> mapType;
    private final int expectedNodes;
    private final int actualNodes;
    private final Optional<String> processingStage;
    private final Optional<String> coordinateContext;

    /**
     * Creates a new MapProcessingException with map context information.
     * 
     * @param message the detail message explaining the map processing failure
     * @param mapType the type of map being processed (e.g., "FullMap", "HalfMap")
     * @param processingStage the stage of processing where the failure occurred
     */
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
    
    /**
     * Creates a new MapProcessingException with node count context.
     * 
     * @param message the detail message explaining the map processing failure
     * @param mapType the type of map being processed
     * @param processingStage the stage of processing where the failure occurred
     * @param expectedNodes the expected number of map nodes
     * @param actualNodes the actual number of map nodes found
     */
    private MapProcessingException(String message, String mapType, String processingStage, int expectedNodes, int actualNodes) {
        super(buildDetailedMessage(message,
                Optional.ofNullable(mapType),
                Optional.ofNullable(processingStage),
                expectedNodes,
                actualNodes,
                Optional.empty()));
        this.mapType = Optional.ofNullable(mapType);
        this.processingStage = Optional.ofNullable(processingStage);
        this.expectedNodes = expectedNodes;
        this.actualNodes = actualNodes;
        this.coordinateContext = Optional.empty();
    }

    /**
     * Creates a new MapProcessingException with complete context and cause.
     * 
     * @param message the detail message explaining the map processing failure
     * @param cause the underlying cause of the map processing failure
     * @param mapType the type of map being processed
     * @param processingStage the stage of processing where the failure occurred
     * @param expectedNodes the expected number of map nodes
     * @param actualNodes the actual number of map nodes found
     * @param coordinateContext information about problematic coordinates
     */
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

    /**
     * Builds a detailed error message with map processing context information.
     */
    private static String buildDetailedMessage(
            String message,
            Optional<String> mapType,
            Optional<String> processingStage,
            int expectedNodes,
            int actualNodes,
            Optional<String> coordinateContext
    ) {
        StringBuilder sb = new StringBuilder("Map Processing Error: ").append(message);
        Objects.requireNonNull(mapType, "mapType is required")
            .filter(value -> !value.isBlank())
            .ifPresent(value -> sb.append(" [Map Type: ").append(value).append("]"));
        Objects.requireNonNull(processingStage, "processingStage is required")
            .filter(value -> !value.isBlank())
            .ifPresent(value -> sb.append(" [Stage: ").append(value).append("]"));
        if (expectedNodes > 0 && actualNodes >= 0) {
            sb.append(" [Expected Nodes: ").append(expectedNodes).append(", Actual: ").append(actualNodes).append("]");
        }
        Objects.requireNonNull(coordinateContext, "coordinateContext is required")
            .filter(value -> !value.isBlank())
            .ifPresent(value -> sb.append(" [Coordinates: ").append(value).append("]"));
        return sb.toString();
    }

    /**
     * Determines if this exception includes node count information.
     * 
     * @return true if both expected and actual node counts are available
     */
    private boolean hasNodeCountInfo() {
        return expectedNodes > 0 && actualNodes >= 0;
    }
    
    /**
     * Determines if this map processing error is potentially recoverable.
     * Recoverable errors include temporary server issues, partial data, etc.
     * 
     * @return true if the error might be recoverable with retry logic
     */
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

    /**
     * Creates a user-friendly error message with recovery suggestions.
     * 
     * @return a formatted error message with suggestions for handling the map issue
     */
    public String getRecoveryMessage() {
        StringBuilder recovery = new StringBuilder();
        recovery.append("🗺️  Map Processing Error: ").append(getMessage()).append("\n");

        mapType.filter(value -> !value.isBlank()).ifPresent(value -> recovery.append("📋 Map Type: ").append(value).append("\n"));
        
        if (hasNodeCountInfo()) {
            recovery.append("📊 Expected ").append(expectedNodes)
                   .append(" nodes, but received ").append(actualNodes).append("\n");
        }
        
        if (isRecoverable()) {
            recovery.append("✅ This error might be recoverable:\n");
            recovery.append("   • Try refreshing the map data\n");
            recovery.append("   • Check network connection\n");
            recovery.append("   • Wait a moment and retry the operation");
        } else {
            recovery.append("⚠️  This likely cannot be recovered in this run:\n");
            recovery.append("   • Verify client/server protocol compatibility\n");
            recovery.append("   • Inspect server ResponseEnvelope exception details (name/message)\n");
            recovery.append("   • Re-run with -Dclient.debug=true for stack traces");
        }
        
        return recovery.toString();
    }
}