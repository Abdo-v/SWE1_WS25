package client.exception;

import java.util.Optional;

/**
 * Checked exception for game communication failures.
 * 
 * This exception is thrown when communication with the game server fails
 * in a recoverable way.
 */
public class GameCommunicationException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private final Optional<String> serverUrl;
    private final Optional<String> operation;
    private final int httpStatusCode;
    private final Optional<String> remoteExceptionName;
    private final Optional<String> remoteExceptionMessage;
    
    /**
     * Creates a new GameCommunicationException with a message.
     * 
     * @param message the detail message explaining the communication failure
     */
    public GameCommunicationException(String message) {
        super(message);
        this.serverUrl = Optional.empty();
        this.operation = Optional.empty();
        this.httpStatusCode = -1;
        this.remoteExceptionName = Optional.empty();
        this.remoteExceptionMessage = Optional.empty();
    }
    
    /**
     * Creates a new GameCommunicationException with a message and cause.
     * 
     * @param message the detail message explaining the communication failure
     * @param cause the underlying cause of the communication failure
     */
    public GameCommunicationException(String message, Throwable cause) {
        super(message, cause);
        this.serverUrl = Optional.empty();
        this.operation = Optional.empty();
        this.httpStatusCode = -1;
        this.remoteExceptionName = Optional.empty();
        this.remoteExceptionMessage = Optional.empty();
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
        this.serverUrl = Optional.ofNullable(serverUrl);
        this.operation = Optional.ofNullable(operation);
        this.httpStatusCode = httpStatusCode;
        this.remoteExceptionName = Optional.empty();
        this.remoteExceptionMessage = Optional.empty();
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
        this.serverUrl = Optional.ofNullable(serverUrl);
        this.operation = Optional.ofNullable(operation);
        this.httpStatusCode = httpStatusCode;
        this.remoteExceptionName = Optional.empty();
        this.remoteExceptionMessage = Optional.empty();
    }

    /**
     * Creates a new GameCommunicationException representing a server-side rejection
     * reported via ResponseEnvelope (exception name + message).
     */
    public GameCommunicationException(
            String message,
            String serverUrl,
            String operation,
            int httpStatusCode,
            String remoteExceptionName,
            String remoteExceptionMessage
    ) {
        super(buildDetailedMessage(message, serverUrl, operation, httpStatusCode));
        this.serverUrl = Optional.ofNullable(serverUrl);
        this.operation = Optional.ofNullable(operation);
        this.httpStatusCode = httpStatusCode;
        this.remoteExceptionName = Optional.ofNullable(remoteExceptionName);
        this.remoteExceptionMessage = Optional.ofNullable(remoteExceptionMessage);
    }

    public GameCommunicationException(String message, String serverUrl, Operation operation, int httpStatusCode) {
        this(message, serverUrl, Optional.ofNullable(operation).map(Operation::code).orElse(""), httpStatusCode);
    }

    public GameCommunicationException(String message, Throwable cause, String serverUrl, Operation operation, int httpStatusCode) {
        this(message, cause, serverUrl, Optional.ofNullable(operation).map(Operation::code).orElse(""), httpStatusCode);
    }
    
    /**
     * Builds a detailed error message with context information.
     */
    private static String buildDetailedMessage(String message, String serverUrl, String operation, int httpStatusCode) {
        StringBuilder sb = new StringBuilder(message);
        Optional.ofNullable(operation).filter(op -> !op.isBlank()).ifPresent(op -> sb.append(TextCnofig.LABEL_BRACKET_OPEN)
                .append(TextCnofig.LABEL_OPERATION).append(op).append(TextCnofig.LABEL_BRACKET_CLOSE));
        Optional.ofNullable(serverUrl).filter(url -> !url.isBlank()).ifPresent(url -> sb.append(TextCnofig.LABEL_BRACKET_OPEN)
                .append(TextCnofig.LABEL_SERVER).append(url).append(TextCnofig.LABEL_BRACKET_CLOSE));
        if (httpStatusCode > 0) {
            sb.append(TextCnofig.LABEL_BRACKET_OPEN).append(TextCnofig.LABEL_HTTP_STATUS).append(httpStatusCode).append(TextCnofig.LABEL_BRACKET_CLOSE);
        }
        return sb.toString();
    }
    
    // Getters for additional context information
    public Optional<String> getServerUrl() { return serverUrl; }
    public Optional<String> getOperation() { return operation; }
    public int getHttpStatusCode() { return httpStatusCode; }
    public Optional<String> getRemoteExceptionName() { return remoteExceptionName; }
    public Optional<String> getRemoteExceptionMessage() { return remoteExceptionMessage; }
    
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