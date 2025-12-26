package client.model.common;

import java.util.Locale;
import java.util.Objects;

/**
 * Central toggle for optional debug output.
 *
 * <p>To enable in CLI runs use one of:
 * <ul>
 *   <li>{@code -Dclient.debug=true}</li>
 *   <li>environment variable {@code CLIENT_DEBUG=true}</li>
 * </ul>
 *
 * <p>Keeping stack traces behind a flag keeps evaluation output readable while
 * still allowing developers to inspect root causes when needed.
 */
public final class DebugSettings {

    public static final String DEBUG_PROPERTY = "client.debug";
    public static final String DEBUG_ENV = "CLIENT_DEBUG";

    private DebugSettings() {
    }

    public static boolean isDebugEnabled() {
        String propertyValue = System.getProperty(DEBUG_PROPERTY);
        if (isTruthy(propertyValue)) {
            return true;
        }

        String envValue = System.getenv(DEBUG_ENV);
        return isTruthy(envValue);
    }

    public static void printStackTraceIfDebug(Throwable throwable) {
        Objects.requireNonNull(throwable, "throwable is required");
        if (!isDebugEnabled()) {
            return;
        }
        throwable.printStackTrace(System.err);
    }

    private static boolean isTruthy(String value) {
        String normalized = Objects.requireNonNullElse(value, "").trim().toLowerCase(Locale.ROOT);
        return normalized.equals("1")
                || normalized.equals("true")
                || normalized.equals("yes")
                || normalized.equals("y")
                || normalized.equals("on");
    }
}
