package client.exception;

/**
 * Checked exception for game communication failures.
 * 
 * This exception is thrown when communication with the game server fails
 * in a recoverable way. Since network issues are often temporary and can
 * be handled by retry logic, fallback mechanisms, or graceful degradation,
 * this is implemented as a checked exception to force calling code to
 * handle these recoverable error conditions.
 * 
 * Use cases:
 * - Server connection timeouts
 * - HTTP request failures (4xx, 5xx responses)
 * - Network connectivity issues
 * - Server temporarily unavailable
 * - Malformed server responses that can be retried
 * 
 * @author Abdalrahman Mohammed
 */
public class GameCommunicationException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private final String serverUrl;
    private final String operation;
    private final int httpStatusCode;
    
    /**
     * Creates a new GameCommunicationException with a message.
     * 
     * @param message the detail message explaining the communication failure
     */
    public GameCommunicationException(String message) {
        super(message);
        this.serverUrl = null;
        this.operation = null;
        this.httpStatusCode = -1;
    }
    
    /**
     * Creates a new GameCommunicationException with a message and cause.
     * 
     * @param message the detail message explaining the communication failure
     * @param cause the underlying cause of the communication failure
     */
    public GameCommunicationException(String message, Throwable cause) {
        super(message, cause);
        this.serverUrl = null;
        this.operation = null;
        this.httpStatusCode = -1;
    }
    
    /**
     * Creates a new GameCommunicationException with detailed context information.
     * 
     * @param message the detail message explaining the communication failure
     * @param serverUrl the server URL that failed
     * @param operation the operation that was being performed
     * @param httpStatusCode the HTTP status code received (if applicable)
     */
    public GameCommunicationException(String message, String serverUrl, String operation, int httpStatusCode) {
        super(buildDetailedMessage(message, serverUrl, operation, httpStatusCode));
        this.serverUrl = serverUrl;
        this.operation = operation;
        this.httpStatusCode = httpStatusCode;
    }
    
    /**
     * Creates a new GameCommunicationException with detailed context information and cause.
     * 
     * @param message the detail message explaining the communication failure
     * @param cause the underlying cause of the communication failure
     * @param serverUrl the server URL that failed
     * @param operation the operation that was being performed
     * @param httpStatusCode the HTTP status code received (if 0)
     */
    public GameCommunicationException(String message, Throwable cause, String serverUrl, String operation, int httpStatusCode) {
        super(buildDetailedMessage(message, serverUrl, operation, httpStatusCode), cause);
        this.serverUrl = serverUrl;
        this.operation = operation;
        this.httpStatusCode = httpStatusCode;
    }

    public GameCommunicationException(String message, String serverUrl, Operation operation, int httpStatusCode) {
        this(message, serverUrl, operation != null ? operation.code() : null, httpStatusCode);
    }

    public GameCommunicationException(String message, Throwable cause, String serverUrl, Operation operation, int httpStatusCode) {
        this(message, cause, serverUrl, operation != null ? operation.code() : null, httpStatusCode);
    }
    
    /**
     * Builds a detailed error message with context information.
     */
    private static String buildDetailedMessage(String message, String serverUrl, String operation, int httpStatusCode) {
        StringBuilder sb = new StringBuilder(message);
        if (operation != null) {
            sb.append(" [Operation: ").append(operation).append("]");
        }
        if (serverUrl != null) {
            sb.append(" [Server: ").append(serverUrl).append("]");
        }
        if (httpStatusCode > 0) {
            sb.append(" [HTTP Status: ").append(httpStatusCode).append("]");
        }
        return sb.toString();
    }
    
    // Getters for additional context information
    public String getServerUrl() { return serverUrl; }
    public String getOperation() { return operation; }
    public int getHttpStatusCode() { return httpStatusCode; }
    
    /**
     * Determines if this communication error is potentially recoverable
     * based on the HTTP status code.
     * 
     * @return true if the error might be recoverable with retry logic
     */
    public boolean isRecoverable() {
        // 5xx server errors are often temporary and recoverable
        // 429 (Too Many Requests) is recoverable with backoff
        // 408 (Request Timeout) is recoverable
        return httpStatusCode >= 500 || httpStatusCode == 429 || httpStatusCode == 408;
    }
}