package client.exception;

import java.util.Optional;

/**
 * Signals failures while communicating with the game server.
 *
 * <p>Includes optional context (server URL, operation, HTTP status, remote exception details)
 * so callers can present a helpful error message without leaking networking code details.
 */
public class GameCommunicationException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private final Optional<String> serverUrl;
    private final Optional<String> operation;
    private final int httpStatusCode;
    private final Optional<String> remoteExceptionName;
    private final Optional<String> remoteExceptionMessage;
    
    public GameCommunicationException(String message) {
        super(message);
        this.serverUrl = Optional.empty();
        this.operation = Optional.empty();
        this.httpStatusCode = -1;
        this.remoteExceptionName = Optional.empty();
        this.remoteExceptionMessage = Optional.empty();
    }
    
    public GameCommunicationException(String message, Throwable cause) {
        super(message, cause);
        this.serverUrl = Optional.empty();
        this.operation = Optional.empty();
        this.httpStatusCode = -1;
        this.remoteExceptionName = Optional.empty();
        this.remoteExceptionMessage = Optional.empty();
    }
    
    public GameCommunicationException(String message, String serverUrl, String operation, int httpStatusCode) {
        super(buildDetailedMessage(message, serverUrl, operation, httpStatusCode));
        this.serverUrl = Optional.ofNullable(serverUrl);
        this.operation = Optional.ofNullable(operation);
        this.httpStatusCode = httpStatusCode;
        this.remoteExceptionName = Optional.empty();
        this.remoteExceptionMessage = Optional.empty();
    }
    
    public GameCommunicationException(String message, Throwable cause, String serverUrl, String operation, int httpStatusCode) {
        super(buildDetailedMessage(message, serverUrl, operation, httpStatusCode), cause);
        this.serverUrl = Optional.ofNullable(serverUrl);
        this.operation = Optional.ofNullable(operation);
        this.httpStatusCode = httpStatusCode;
        this.remoteExceptionName = Optional.empty();
        this.remoteExceptionMessage = Optional.empty();
    }

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
    
    public Optional<String> getServerUrl() { return serverUrl; }
    public Optional<String> getOperation() { return operation; }
    public int getHttpStatusCode() { return httpStatusCode; }
    public Optional<String> getRemoteExceptionName() { return remoteExceptionName; }
    public Optional<String> getRemoteExceptionMessage() { return remoteExceptionMessage; }

    public boolean isRecoverable() {
        return httpStatusCode >= 500 || httpStatusCode == 429 || httpStatusCode == 408;
    }
}