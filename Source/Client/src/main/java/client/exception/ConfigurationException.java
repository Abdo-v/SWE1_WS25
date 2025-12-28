package client.exception;

import java.util.Optional;

/**
 * Signals invalid or missing configuration values (typically CLI arguments).
 *
 * <p>Includes optional context for user-friendly reporting (key, provided value, valid values).
 */
public class ConfigurationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final Optional<String> configurationKey;
    private final Optional<String> providedValue;
    private final String[] validValues;
    
    public ConfigurationException(String message) {
        super(message);
        this.configurationKey = Optional.empty();
        this.providedValue = Optional.empty();
        this.validValues = new String[0];
    }
    
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
        this.configurationKey = Optional.empty();
        this.providedValue = Optional.empty();
        this.validValues = new String[0];
    }
    
    public ConfigurationException(String message, String configurationKey, String providedValue, String[] validValues) {
        super(message);
        this.configurationKey = Optional.ofNullable(configurationKey);
        this.providedValue = Optional.ofNullable(providedValue);
        this.validValues = Optional.ofNullable(validValues).map(String[]::clone).orElseGet(() -> new String[0]);
    }
    
    public ConfigurationException(String message, Throwable cause, String configurationKey, String providedValue, String[] validValues) {
        super(message, cause);
        this.configurationKey = Optional.ofNullable(configurationKey);
        this.providedValue = Optional.ofNullable(providedValue);
        this.validValues = Optional.ofNullable(validValues).map(String[]::clone).orElseGet(() -> new String[0]);
    }
    
    public Optional<String> getConfigurationKey() { return configurationKey; }
    public Optional<String> getProvidedValue() { return providedValue; }
    public String[] getValidValues() { return validValues.clone(); }

    public boolean hasValidValues() {
        return validValues.length > 0;
    }
}