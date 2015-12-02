package com.sqrt4.jircd.sched;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class Task<T> implements Callable<T>, Runnable {
    public final void run() {
        try {
            call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final Future<T> submit() {
        return Scheduler.submit(this);
    }

    public final ScheduledFuture<T> schedule(long delay, TimeUnit unit) {
        return Scheduler.schedule(this, delay, unit);
    }

    public final ScheduledFuture<?> scheduleAtFixedRate(long initialDelay, long delay, TimeUnit unit) {
        return Scheduler.schedulAtFixedRate(this, initialDelay, delay, unit);
    }

    public final ScheduledFuture<?> scheduleWithFixedDelay(long initialDelay, long delay, TimeUnit unit) {
        return Scheduler.scheduleWithFixedDelay(this, initialDelay, delay, unit);
    }
}