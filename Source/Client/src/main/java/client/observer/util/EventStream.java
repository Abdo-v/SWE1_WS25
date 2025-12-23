package client.observer.util;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Simple, reusable, generic observer implementation.
 *
 * <p>Designed for modern Java usage: generics + lambdas ({@link Consumer}) + composition.
 * Thread-safe for iteration via {@link CopyOnWriteArrayList}.</p>
 */
public final class EventStream<E> implements EventSource<E> {

    private final CopyOnWriteArrayList<Consumer<? super E>> listeners = new CopyOnWriteArrayList<>();

    @Override
    public Subscription subscribe(Consumer<? super E> listener) {
        Objects.requireNonNull(listener, "listener is required");
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    /** Publish an event to all subscribers. */
    public void publish(E event) {
        Objects.requireNonNull(event, "event is required");
        for (Consumer<? super E> listener : listeners) {
            listener.accept(event);
        }
    }
}
