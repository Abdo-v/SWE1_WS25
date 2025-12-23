package client.observer.util;

import java.util.function.Consumer;

/**
 * Read-only view of an event stream.
 *
 * @param <E> the event payload type
 */
public interface EventSource<E> {

    /**
     * Subscribe a listener (often provided as a lambda expression).
     *
     * @param listener event listener
     * @return a {@link Subscription} to unsubscribe
     */
    Subscription subscribe(Consumer<? super E> listener);
}
