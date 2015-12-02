package com.sqrt4.jircd.sched;

import java.util.concurrent.*;

public class Scheduler {
    private static Scheduler scheduler = new Scheduler();
    private ScheduledExecutorService sched;

    public Scheduler() {
        int cores = Runtime.getRuntime().availableProcessors();
        sched = Executors.newScheduledThreadPool(cores);
    }

    private <T> Future<T> _submit(Callable<T> c) {
        return sched.submit(c);
    }

    private <T> ScheduledFuture<T> _schedule(Callable<T> task, long delay, TimeUnit unit) {
        return sched.schedule(task, delay, unit);
    }

    private ScheduledFuture<?> _scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return sched.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    private ScheduledFuture<?> _scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
        return sched.scheduleWithFixedDelay(task, initialDelay, delay, unit);
    }

    public static <T> Future<T> submit(Callable<T>  task) {
        return scheduler._submit(task);
    }

    public static <T> ScheduledFuture<T> schedule(Callable<T> task, long delay, TimeUnit unit) {
        return scheduler._schedule(task, delay, unit);
    }

    public static ScheduledFuture<?> schedulAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return scheduler._scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
        return scheduler._scheduleWithFixedDelay(task, initialDelay, delay, unit);
    }
}