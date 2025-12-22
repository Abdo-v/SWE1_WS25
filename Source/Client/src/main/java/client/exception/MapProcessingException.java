package client.exception;

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
    
    private final String mapType;
    private final int expectedNodes;
    private final int actualNodes;
    private final String processingStage;
    private final String coordinateContext;
    
    /**
     * Creates a new MapProcessingException with a message.
     * 
     * @param message the detail message explaining the map processing failure
     */
    public MapProcessingException(String message) {
        super(message);
        this.mapType = null;
        this.processingStage = null;
        this.expectedNodes = -1;
        this.actualNodes = -1;
        this.coordinateContext = null;
    }
    
    /**
     * Creates a new MapProcessingException with a message and cause.
     * 
     * @param message the detail message explaining the map processing failure
     * @param cause the underlying cause of the map processing failure
     */
    public MapProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.mapType = null;
        this.processingStage = null;
        this.expectedNodes = -1;
        this.actualNodes = -1;
        this.coordinateContext = null;
    }
    
    /**
     * Creates a new MapProcessingException with map context information.
     * 
     * @param message the detail message explaining the map processing failure
     * @param mapType the type of map being processed (e.g., "FullMap", "HalfMap")
     * @param processingStage the stage of processing where the failure occurred
     */
    public MapProcessingException(String message, String mapType, String processingStage) {
        super(buildDetailedMessage(message, mapType, processingStage, -1, -1, null));
        this.mapType = mapType;
        this.processingStage = processingStage;
        this.expectedNodes = -1;
        this.actualNodes = -1;
        this.coordinateContext = null;
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
        super(buildDetailedMessage(message, mapType, processingStage, expectedNodes, actualNodes, null));
        this.mapType = mapType;
        this.processingStage = processingStage;
        this.expectedNodes = expectedNodes;
        this.actualNodes = actualNodes;
        this.coordinateContext = null;
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
        super(buildDetailedMessage(message, mapType, processingStage, -1, -1, coordinateContext));
        this.mapType = mapType;
        this.processingStage = processingStage;
        this.expectedNodes = -1;
        this.actualNodes = -1;
        this.coordinateContext = coordinateContext;
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
        super(buildDetailedMessage(message, mapType, processingStage, expectedNodes, actualNodes, coordinateContext), cause);
        this.mapType = mapType;
        this.processingStage = processingStage;
        this.expectedNodes = expectedNodes;
        this.actualNodes = actualNodes;
        this.coordinateContext = coordinateContext;
    }

    public MapProcessingException(String message, MapDataType mapType, MapProcessingStage processingStage) {
        this(message,
            mapType != null ? mapType.label() : null,
            processingStage != null ? processingStage.code() : null);
    }

    public MapProcessingException(String message, MapDataType mapType, MapProcessingStage processingStage, int expectedNodes, int actualNodes) {
        this(message,
            mapType != null ? mapType.label() : null,
            processingStage != null ? processingStage.code() : null,
            expectedNodes,
            actualNodes);
    }

    public MapProcessingException(String message, Throwable cause, MapDataType mapType, MapProcessingStage processingStage,
                                 int expectedNodes, int actualNodes, String coordinateContext) {
        this(message,
            cause,
            mapType != null ? mapType.label() : null,
            processingStage != null ? processingStage.code() : null,
            expectedNodes,
            actualNodes,
            coordinateContext);
    }
    
    /**
     * Builds a detailed error message with map processing context information.
     */
    private static String buildDetailedMessage(String message, String mapType, String processingStage, 
                                             int expectedNodes, int actualNodes, String coordinateContext) {
        StringBuilder sb = new StringBuilder("Map Processing Error: ").append(message);
        if (mapType != null) {
            sb.append(" [Map Type: ").append(mapType).append("]");
        }
        if (processingStage != null) {
            sb.append(" [Stage: ").append(processingStage).append("]");
        }
        if (expectedNodes > 0 && actualNodes >= 0) {
            sb.append(" [Expected Nodes: ").append(expectedNodes).append(", Actual: ").append(actualNodes).append("]");
        }
        if (coordinateContext != null) {
            sb.append(" [Coordinates: ").append(coordinateContext).append("]");
        }
        return sb.toString();
    }
    
    // Getters for additional context information
    public String getMapType() { return mapType; }
    public String getProcessingStage() { return processingStage; }
    public int getExpectedNodes() { return expectedNodes; }
    public int getActualNodes() { return actualNodes; }
    public String getCoordinateContext() { return coordinateContext; }
    
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
        return coordinateContext != null && !coordinateContext.trim().isEmpty();
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
        
        if (processingStage != null) {
            String stage = processingStage.toLowerCase();
            return stage.contains("server") || stage.contains("network") || stage.contains("conversion");
        }
        
        return false;
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
        report.append("Map Type: ").append(mapType != null ? mapType : "Unknown").append("\n");
        report.append("Processing Stage: ").append(processingStage != null ? processingStage : "Unknown").append("\n");
        
        if (hasNodeCountInfo()) {
            report.append("Node Count Issue: Expected ").append(expectedNodes)
                  .append(", but found ").append(actualNodes).append("\n");
        }
        
        if (hasCoordinateInfo()) {
            report.append("Coordinate Context: ").append(coordinateContext).append("\n");
        }
        
        report.append("Recoverable: ").append(isRecoverable() ? "Yes" : "No").append("\n");
        
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
     * Creates a user-friendly error message with recovery suggestions.
     * 
     * @return a formatted error message with suggestions for handling the map issue
     */
    public String getRecoveryMessage() {
        StringBuilder recovery = new StringBuilder();
        recovery.append("🗺️  Map Processing Error: ").append(getMessage()).append("\n");
        
        if (mapType != null) {
            recovery.append("📋 Map Type: ").append(mapType).append("\n");
        }
        
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