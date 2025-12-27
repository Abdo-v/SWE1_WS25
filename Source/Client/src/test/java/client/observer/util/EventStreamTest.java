package client.observer.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link EventStream} subscribe/unsubscribe behavior and publication semantics.
 */
class EventStreamTest {

    @Test
    void publish_notifiesAllSubscribers_inSubscriptionOrder() {
        EventStream<String> stream = new EventStream<>();

        List<String> received = new ArrayList<>();
        stream.subscribe(e -> received.add("first:" + e));
        stream.subscribe(e -> received.add("second:" + e));

        stream.publish("x");

        assertEquals(List.of("first:x", "second:x"), received);
    }

    @Test
    void unsubscribe_stopsFurtherNotifications_andIsIdempotent() {
        EventStream<Integer> stream = new EventStream<>();

        AtomicInteger calls = new AtomicInteger(0);
        Subscription sub = stream.subscribe(e -> calls.incrementAndGet());

        stream.publish(1);
        assertEquals(1, calls.get());

        sub.unsubscribe();
        sub.unsubscribe();

        stream.publish(2);
        assertEquals(1, calls.get(), "Listener must not be called after unsubscribe");
    }

    @Test
    void close_onSubscription_unsubscribes_forTryWithResourcesUsage() {
        EventStream<String> stream = new EventStream<>();
        List<String> received = new ArrayList<>();

        try (Subscription ignored = stream.subscribe(received::add)) {
            stream.publish("a");
        }

        stream.publish("b");
        assertEquals(List.of("a"), received);
    }

    @Test
    void publish_allowsSubscribeDuringPublish_newListenerReceivesOnlyFutureEvents() {
        EventStream<String> stream = new EventStream<>();

        List<String> received = new ArrayList<>();

        AtomicReference<Optional<Subscription>> secondSubscription = new AtomicReference<>(Optional.empty());
        stream.subscribe(e -> {
            received.add("first:" + e);
            if (secondSubscription.get().isEmpty()) {
                secondSubscription.set(Optional.of(stream.subscribe(e2 -> received.add("second:" + e2))));
            }
        });

        stream.publish("one");
        stream.publish("two");

        assertEquals(List.of("first:one", "first:two", "second:two"), received);
    }
}
