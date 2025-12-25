package client.exception;

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
        super(message);
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
        super(message, cause);
        this.configurationKey = Optional.ofNullable(configurationKey);
        this.providedValue = Optional.ofNullable(providedValue);
        this.validValues = Optional.ofNullable(validValues).map(String[]::clone).orElseGet(() -> new String[0]);
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
}