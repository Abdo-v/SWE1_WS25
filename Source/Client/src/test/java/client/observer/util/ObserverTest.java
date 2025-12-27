package client.observer.util;

import client.model.GameState;
import client.model.GameStateEvent;
import client.model.GameStateEventType;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link Observer} default typed-event adapter behavior.
 */
class ObserverTest {

    @Test
    void update_whenCalledWithGameStateEvent_delegatesToUpdateGameState() {
        GameState state = new GameState("g1");
        GameStateEvent event = new GameStateEvent(state, GameStateEventType.BULK_UPDATE);

        AtomicReference<GameState> seen = new AtomicReference<>();
        Observer observer = seen::set;

        observer.update(event);

        assertSame(state, seen.get());
    }

    @Test
    void update_whenEventIsNull_throws() {
        Observer observer = gs -> { };
        assertThrows(NullPointerException.class, () -> observer.update((GameStateEvent) null));
    }
}
