package client.exception;

import java.util.Optional;

/**
 * Checked exception for map data processing failures.
 * 
 * This exception is thrown when map data cannot be properly processed,
 * converted, or validated. Since map issues might be recoverable through
 * re-requesting data or using cached versions, this is a checked exception
 * that forces calling code to handle these scenarios appropriately.
 * 
 * Use cases:
 * - Server map data corruption or inconsistency
 * - Map conversion failures between server and client formats
 * - Invalid map node counts or structure
 * - Map coordinate validation failures
 * - Terrain type conversion errors
 * 
 * @author Abdalrahman Mohammed
 */
public class MapProcessingException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private final Optional<String> mapType;
    private final int expectedNodes;
    private final int actualNodes;
    private final Optional<String> processingStage;
    private final Optional<String> coordinateContext;
    
    /**
     * Creates a new MapProcessingException with a message.
     * 
     * @param message the detail message explaining the map processing failure
     */
    public MapProcessingException(String message) {
        super(message);
        this.mapType = Optional.empty();
        this.processingStage = Optional.empty();
        this.expectedNodes = -1;
        this.actualNodes = -1;
        this.coordinateContext = Optional.empty();
    }
    
    /**
     * Creates a new MapProcessingException with a message and cause.
     * 
     * @param message the detail message explaining the map processing failure
     * @param cause the underlying cause of the map processing failure
     */
    public MapProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.mapType = Optional.empty();
        this.processingStage = Optional.empty();
        this.expectedNodes = -1;
        this.actualNodes = -1;
        this.coordinateContext = Optional.empty();
    }
    
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
    public MapProcessingException(String message, String mapType, String processingStage, int expectedNodes, int actualNodes) {
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
     * Creates a new MapProcessingException with coordinate context.
     * 
     * @param message the detail message explaining the map processing failure
     * @param mapType the type of map being processed
     * @param processingStage the stage of processing where the failure occurred
     * @param coordinateContext information about the problematic coordinates
     */
    public MapProcessingException(String message, String mapType, String processingStage, String coordinateContext) {
        super(buildDetailedMessage(message,
                Optional.ofNullable(mapType),
                Optional.ofNullable(processingStage),
                -1,
                -1,
                Optional.ofNullable(coordinateContext)));
        this.mapType = Optional.ofNullable(mapType);
        this.processingStage = Optional.ofNullable(processingStage);
        this.expectedNodes = -1;
        this.actualNodes = -1;
        this.coordinateContext = Optional.ofNullable(coordinateContext);
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

    public MapProcessingException(String message, MapDataType mapType, MapProcessingStage processingStage) {
        this(message,
            Optional.ofNullable(mapType).map(MapDataType::label).orElse(""),
            Optional.ofNullable(processingStage).map(MapProcessingStage::code).orElse(""));
    }

    public MapProcessingException(String message, MapDataType mapType, MapProcessingStage processingStage, int expectedNodes, int actualNodes) {
        this(message,
            Optional.ofNullable(mapType).map(MapDataType::label).orElse(""),
            Optional.ofNullable(processingStage).map(MapProcessingStage::code).orElse(""),
            expectedNodes,
            actualNodes);
    }

    public MapProcessingException(String message, Throwable cause, MapDataType mapType, MapProcessingStage processingStage,
                                 int expectedNodes, int actualNodes, String coordinateContext) {
        this(message,
            cause,
            Optional.ofNullable(mapType).map(MapDataType::label).orElse(""),
            Optional.ofNullable(processingStage).map(MapProcessingStage::code).orElse(""),
            expectedNodes,
            actualNodes,
            coordinateContext);
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
        Optional.ofNullable(mapType).orElse(Optional.empty())
                .filter(value -> !value.isBlank())
                .ifPresent(value -> sb.append(" [Map Type: ").append(value).append("]"));
        Optional.ofNullable(processingStage).orElse(Optional.empty())
                .filter(value -> !value.isBlank())
                .ifPresent(value -> sb.append(" [Stage: ").append(value).append("]"));
        if (expectedNodes > 0 && actualNodes >= 0) {
            sb.append(" [Expected Nodes: ").append(expectedNodes).append(", Actual: ").append(actualNodes).append("]");
        }
        Optional.ofNullable(coordinateContext).orElse(Optional.empty())
                .filter(value -> !value.isBlank())
                .ifPresent(value -> sb.append(" [Coordinates: ").append(value).append("]"));
        return sb.toString();
    }
    
    // Getters for additional context information
    public Optional<String> getMapType() { return mapType; }
    public Optional<String> getProcessingStage() { return processingStage; }
    public int getExpectedNodes() { return expectedNodes; }
    public int getActualNodes() { return actualNodes; }
    public Optional<String> getCoordinateContext() { return coordinateContext; }
    
    /**
     * Determines if this exception includes node count information.
     * 
     * @return true if both expected and actual node counts are available
     */
    public boolean hasNodeCountInfo() {
        return expectedNodes > 0 && actualNodes >= 0;
    }
    
    /**
     * Determines if this exception includes coordinate information.
     * 
     * @return true if coordinate context is available
     */
    public boolean hasCoordinateInfo() {
        return coordinateContext.filter(text -> !text.trim().isEmpty()).isPresent();
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
     * Creates a formatted debug report for developers.
     * 
     * @return a detailed debug report of the map processing failure
     */
    public String getDebugReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== MAP PROCESSING EXCEPTION DEBUG REPORT ===\n");
        report.append("Message: ").append(getMessage()).append("\n");
        report.append("Map Type: ").append(mapType.orElse("Unknown")).append("\n");
        report.append("Processing Stage: ").append(processingStage.orElse("Unknown")).append("\n");
        
        if (hasNodeCountInfo()) {
            report.append("Node Count Issue: Expected ").append(expectedNodes)
                  .append(", but found ").append(actualNodes).append("\n");
        }
        
        if (hasCoordinateInfo()) {
            report.append("Coordinate Context: ").append(coordinateContext.orElse("Unknown")).append("\n");
        }
        
        report.append("Recoverable: ").append(isRecoverable() ? "Yes" : "No").append("\n");
        
        Optional.ofNullable(getCause()).ifPresent(cause -> report
            .append("Underlying Cause: ").append(cause.getClass().getSimpleName())
            .append(" - ").append(cause.getMessage()).append("\n"));
        
        report.append("Timestamp: ").append(java.time.LocalDateTime.now()).append("\n");
        report.append("Stack Trace: Available via printStackTrace()").append("\n");
        report.append("=== END DEBUG REPORT ===");
        return report.toString();
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
            recovery.append("⚠️  This appears to be a permanent issue:\n");
            recovery.append("   • Check server compatibility\n");
            recovery.append("   • Verify map data format\n");
            recovery.append("   • Contact support if the problem persists");
        }
        
        return recovery.toString();
    }
}