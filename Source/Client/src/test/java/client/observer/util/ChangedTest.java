package client.observer.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link Changed} basic construction behavior.
 */
class ChangedTest {

    @Test
    void constructor_storesOldAndNewValues() {
        Changed<String> changed = new Changed<>("old", "new");
        assertEquals("old", changed.oldValue());
        assertEquals("new", changed.newValue());
    }
}
