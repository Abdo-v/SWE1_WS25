package client.view;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

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
    private ScheduledFuture<?> pending;

    CLIDebouncedRenderScheduler(long debounceMillis) {
        if (debounceMillis < 0) {
            throw new IllegalArgumentException("debounceMillis must be >= 0");
        }
        this.debounceMillis = debounceMillis;
    }

    void schedule(Runnable task) {
        Objects.requireNonNull(task, "task must not be null");
        synchronized (lock) {
            if (pending != null) {
                pending.cancel(false);
            }
            pending = EXECUTOR.schedule(task, debounceMillis, TimeUnit.MILLISECONDS);
        }
    }

    void cancelPending() {
        synchronized (lock) {
            if (pending != null) {
                pending.cancel(false);
                pending = null;
            }
        }
    }
}
