package client.view;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Coalesces bursts of render requests into a single scheduled render.
 *
 * <p>This is used by the dynamic CLI view to avoid flicker and excessive console writes
 * when the model emits multiple updates in quick succession.
 *
 * <p>Thread-safety: {@link #schedule(Runnable)} and {@link #cancelPending()} are safe to call
 * from arbitrary threads; at most one pending task is kept.
 */
final class CLIDebouncedRenderScheduler {

    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread t = new Thread(runnable, "CLIDebouncedRenderScheduler");
            t.setDaemon(true);
            return t;
        }
    });

    private final long debounceMillis;
    private final Object lock = new Object();
    private Optional<ScheduledFuture<?>> pending = Optional.empty();

    CLIDebouncedRenderScheduler(long debounceMillis) {
        if (debounceMillis < 0) {
            throw new IllegalArgumentException("debounceMillis must be >= 0");
        }
        this.debounceMillis = debounceMillis;
    }

    void schedule(Runnable task) {
        Objects.requireNonNull(task, "task is required");
        synchronized (lock) {
            pending.ifPresent(existing -> existing.cancel(false));
            pending = Optional.of(EXECUTOR.schedule(task, debounceMillis, TimeUnit.MILLISECONDS));
        }
    }

    void cancelPending() {
        synchronized (lock) {
            pending.ifPresent(existing -> existing.cancel(false));
            pending = Optional.empty();
        }
    }
}
