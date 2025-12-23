package client.observer.util;

import java.util.Objects;

/**
 * Generic "old -> new" change payload.
 *
 * <p>Works well with immutable values, {@link java.util.Optional}, and snapshots (e.g. {@code List.copyOf}).</p>
 */
public record Changed<T>(T oldValue, T newValue) {

    public Changed {
        Objects.requireNonNull(oldValue, "oldValue must not be null");
        Objects.requireNonNull(newValue, "newValue must not be null");
    }
}
