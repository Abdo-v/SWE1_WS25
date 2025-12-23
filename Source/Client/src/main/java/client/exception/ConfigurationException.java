package client.exception;

import java.util.Arrays;
import java.util.Optional;

/**
 * Unchecked exception for application configuration failures.
 * 
 * This exception is thrown when the application encounters invalid
 * configuration parameters, malformed arguments, or missing required
 * configuration values. Since these represent setup/deployment issues
 * rather than runtime conditions, this is an unchecked exception.
 * 
 * Use cases:
 * - Invalid command line arguments
 * - Missing required configuration parameters
 * - Malformed URLs or file paths
 * - Invalid log levels or game modes
 * - Conflicting configuration options
 * 
 * @author Abdalrahman Mohammed
 */
public class ConfigurationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final Optional<String> configurationKey;
    private final Optional<String> providedValue;
    private final String[] validValues;
    
    /**
     * Creates a new ConfigurationException with a message.
     * 
     * @param message the detail message explaining the configuration failure
     */
    public ConfigurationException(String message) {
        super(message);
        this.configurationKey = Optional.empty();
        this.providedValue = Optional.empty();
        this.validValues = new String[0];
    }
    
    /**
     * Creates a new ConfigurationException with a message and cause.
     * 
     * @param message the detail message explaining the configuration failure
     * @param cause the underlying cause of the configuration failure
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
        this.configurationKey = Optional.empty();
        this.providedValue = Optional.empty();
        this.validValues = new String[0];
    }
    
    /**
     * Creates a new ConfigurationException with detailed configuration context.
     * 
     * @param message the detail message explaining the configuration failure
     * @param configurationKey the configuration parameter that failed
     * @param providedValue the invalid value that was provided
     * @param validValues array of valid values for this configuration parameter
     */
    public ConfigurationException(String message, String configurationKey, String providedValue, String[] validValues) {
        super(buildDetailedMessage(message, configurationKey, providedValue, validValues));
        this.configurationKey = Optional.ofNullable(configurationKey);
        this.providedValue = Optional.ofNullable(providedValue);
        this.validValues = Optional.ofNullable(validValues).map(String[]::clone).orElseGet(() -> new String[0]);
    }
    
    /**
     * Creates a new ConfigurationException with detailed context and cause.
     * 
     * @param message the detail message explaining the configuration failure
     * @param cause the underlying cause of the configuration failure
     * @param configurationKey the configuration parameter that failed
     * @param providedValue the invalid value that was provided
     * @param validValues array of valid values for this configuration parameter
     */
    public ConfigurationException(String message, Throwable cause, String configurationKey, String providedValue, String[] validValues) {
        super(buildDetailedMessage(message, configurationKey, providedValue, validValues), cause);
        this.configurationKey = Optional.ofNullable(configurationKey);
        this.providedValue = Optional.ofNullable(providedValue);
        this.validValues = Optional.ofNullable(validValues).map(String[]::clone).orElseGet(() -> new String[0]);
    }
    
    /**
     * Builds a detailed error message with configuration context information.
     */
    private static String buildDetailedMessage(String message, String configurationKey, String providedValue, String[] validValues) {
        StringBuilder sb = new StringBuilder("Configuration Error: ").append(message);
        Optional.ofNullable(configurationKey).ifPresent(key -> sb.append(" [Parameter: ").append(key).append("]"));
        Optional.ofNullable(providedValue).ifPresent(value -> sb.append(" [Provided: ").append(value).append("]"));

        String[] safeValidValues = Optional.ofNullable(validValues).orElseGet(() -> new String[0]);
        if (safeValidValues.length > 0) {
            sb.append(" [Valid options: ").append(String.join(", ", safeValidValues)).append("]");
        }
        return sb.toString();
    }
    
    // Getters for additional context information
    public Optional<String> getConfigurationKey() { return configurationKey; }
    public Optional<String> getProvidedValue() { return providedValue; }
    public String[] getValidValues() { return validValues.clone(); }
    
    /**
     * Determines if this configuration error has suggested valid values.
     * 
     * @return true if valid values are available for correction
     */
    public boolean hasValidValues() {
        return validValues.length > 0;
    }
    
    /**
     * Creates a user-friendly error message with correction suggestions.
     * 
     * @return a formatted error message with suggestions for fixing the configuration
     */
    public String getHelpMessage() {
        StringBuilder help = new StringBuilder();
        help.append("❌ Configuration Error: ").append(getMessage()).append("\n");

        configurationKey.ifPresent(key -> help.append("📋 Parameter: ").append(key).append("\n"));
        providedValue.ifPresent(value -> help.append("🔍 You provided: ").append(value).append("\n"));
        
        if (hasValidValues()) {
            help.append("✅ Valid options are: ").append(String.join(", ", validValues)).append("\n");
            help.append("💡 Try using one of the valid options listed above.");
        } else {
            help.append("💡 Please check the documentation for valid configuration values.");
        }
        
        return help.toString();
    }
}