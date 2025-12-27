package client.observer.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link Changed} fail-fast invariants (null is not allowed for old/new values).
 */
class ChangedTest {

    @Test
    void constructor_rejectsNullOldValue() {
        assertThrows(NullPointerException.class, () -> new Changed<>(null, "new"));
    }

    @Test
    void constructor_rejectsNullNewValue() {
        assertThrows(NullPointerException.class, () -> new Changed<>("old", null));
    }
}
