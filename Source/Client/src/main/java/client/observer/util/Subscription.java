package client.observer.util;

/**
 * Handle returned when subscribing to an {@link EventSource}.
 *
 * <p>Allows explicit unsubscription and supports try-with-resources via {@link #close()}.</p>
 */
@FunctionalInterface
public interface Subscription extends AutoCloseable {

    /** Unsubscribe the listener. This operation should be idempotent. */
    void unsubscribe();

    @Override
    default void close() {
        unsubscribe();
    }
}
