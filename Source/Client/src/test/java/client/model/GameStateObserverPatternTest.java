package client.model;

import client.observer.util.Observer;
import client.view.testsupport.StdIoCapture;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link GameState} observer registration/notification semantics (global + event-type observers).
 */
class GameStateObserverPatternTest {

    @Test
    void addObserver_whenAddedFirstTime_receivesImmediateBulkUpdate_once() {
        GameState state = new GameState("g1");

        List<GameStateEventType> receivedTypes = new ArrayList<>();
        Observer obs = new Observer() {
            @Override
            public void update(GameState gameState) {
                // Not used in this test.
            }

            @Override
            public void update(GameStateEvent event) {
                receivedTypes.add(event.type());
            }
        };

        state.addObserver(obs);
        state.addObserver(obs);

        assertEquals(List.of(GameStateEventType.BULK_UPDATE), receivedTypes);
    }

    @Test
    void addObserver_forSpecificType_receivesImmediateTypedUpdate() {
        GameState state = new GameState("g1");

        List<GameStateEventType> receivedTypes = new ArrayList<>();
        Observer obs = new Observer() {
            @Override
            public void update(GameState gameState) {
                // Not used in this test.
            }

            @Override
            public void update(GameStateEvent event) {
                receivedTypes.add(event.type());
            }
        };

        state.addObserver(GameStateEventType.MAP_CHANGED, obs);

        assertEquals(List.of(GameStateEventType.MAP_CHANGED), receivedTypes);
    }

    @Test
    void notifyObservers_forType_notifiesGlobalAndTypedObservers_onlyForThatType() {
        GameState state = new GameState("g1");

        List<String> calls = new ArrayList<>();

        Observer global = new Observer() {
            @Override
            public void update(GameState gameState) {
                // Not used in this test.
            }

            @Override
            public void update(GameStateEvent event) {
                calls.add("global:" + event.type());
            }
        };

        Observer mapOnly = new Observer() {
            @Override
            public void update(GameState gameState) {
                // Not used in this test.
            }

            @Override
            public void update(GameStateEvent event) {
                calls.add("map:" + event.type());
            }
        };

        Observer playersOnly = new Observer() {
            @Override
            public void update(GameState gameState) {
                // Not used in this test.
            }

            @Override
            public void update(GameStateEvent event) {
                calls.add("players:" + event.type());
            }
        };

        state.addObserver(global);
        state.addObserver(GameStateEventType.MAP_CHANGED, mapOnly);
        state.addObserver(GameStateEventType.PLAYERS_CHANGED, playersOnly);

        calls.clear();
        state.notifyObservers(GameStateEventType.MAP_CHANGED);

        assertEquals(List.of("global:MAP_CHANGED", "map:MAP_CHANGED"), calls);
    }

    @Test
    void removeObserver_removesFromAllBuckets_andStopsNotifications() {
        GameState state = new GameState("g1");

        AtomicInteger calls = new AtomicInteger();
        Observer obs = gameState -> calls.incrementAndGet();

        state.addObserver(obs);
        state.addObserver(GameStateEventType.MAP_CHANGED, obs);

        // Registration triggers an immediate notification (by design). We only care that removal
        // prevents any subsequent notifications.
        calls.set(0);

        // Remove should clear both global and typed registrations.
        state.removeObserver(obs);

        state.notifyObservers();
        state.notifyObservers(GameStateEventType.MAP_CHANGED);

        assertEquals(0, calls.get());
    }

    @Test
    void notifyObservers_whenOneObserverThrows_stillNotifiesOthers() {
        try (StdIoCapture io = new StdIoCapture()) {
            GameState state = new GameState("g1");

            AtomicInteger goodCalls = new AtomicInteger();

            Observer bad = new Observer() {
                @Override
                public void update(GameState gameState) {
                    // Not used.
                }

                @Override
                public void update(GameStateEvent event) {
                    throw new IllegalStateException("boom");
                }
            };

            Observer good = gameState -> goodCalls.incrementAndGet();

            state.addObserver(bad);
            state.addObserver(good);

            goodCalls.set(0);
            state.notifyObservers();

            assertEquals(1, goodCalls.get());
            assertEquals("", io.stderr(), "This test intentionally triggers a failing observer, but should not pollute the test console with WARN output.");
        }
    }

    @Test
    void notifyObservers_preservesDeterministicOrder_globalsThenTyped() {
        GameState state = new GameState("g1");

        List<String> order = new ArrayList<>();
        Observer g1 = gameState -> order.add("g1");
        Observer g2 = gameState -> order.add("g2");
        Observer t1 = gameState -> order.add("t1");
        Observer t2 = gameState -> order.add("t2");

        state.addObserver(g1);
        state.addObserver(g2);
        state.addObserver(GameStateEventType.MAP_CHANGED, t1);
        state.addObserver(GameStateEventType.MAP_CHANGED, t2);

        order.clear();
        state.notifyObservers(GameStateEventType.MAP_CHANGED);

        assertEquals(List.of("g1", "g2", "t1", "t2"), order);
    }
}
