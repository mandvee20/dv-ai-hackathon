package com.example.roomsimulator.simulator;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Owns the "leaked" worker threads of one room's DVC.
 *
 * With realThreads=true every leaked thread is a real, parked daemon thread in a pool that only
 * this simulator uses, so the JVM thread count really moves. Releasing them cancels our own tasks;
 * no JVM or framework thread is ever touched. With realThreads=false only a counter moves.
 */
public class ThreadLeakSimulator {

    private static final long WORKER_STACK_BYTES = 64 * 1024;

    private final String roomId;
    private final boolean realThreads;
    private final ThreadPoolExecutor executor;
    private final Deque<Future<?>> leaked = new ConcurrentLinkedDeque<>();
    private final AtomicInteger simulatedCount = new AtomicInteger();

    public ThreadLeakSimulator(String roomId, int maxThreads, boolean realThreads) {
        this.roomId = roomId;
        this.realThreads = realThreads;
        this.executor = realThreads
                ? new ThreadPoolExecutor(0, Math.max(1, maxThreads), 200, TimeUnit.MILLISECONDS,
                        new SynchronousQueue<>(), workerFactory(roomId))
                : null;
    }

    /** Adds up to {@code count} threads, never beyond {@code cap} leaked threads. Returns how many were added. */
    public synchronized int add(int count, int cap) {
        int toAdd = Math.max(0, Math.min(count, cap - leakedCount()));
        for (int i = 0; i < toAdd; i++) {
            if (realThreads) {
                leaked.add(executor.submit(ThreadLeakSimulator::parkUntilReleased));
            } else {
                simulatedCount.incrementAndGet();
            }
        }
        return toAdd;
    }

    /** Gracefully cancels every simulator-owned leaked task. Returns how many were released. */
    public synchronized int releaseAll() {
        int released = leakedCount();
        if (realThreads) {
            Future<?> task;
            while ((task = leaked.poll()) != null) {
                task.cancel(true);
            }
        } else {
            simulatedCount.set(0);
        }
        return released;
    }

    public int leakedCount() {
        return realThreads ? leaked.size() : simulatedCount.get();
    }

    /** Live threads in this room's own pool (real JVM number, 0 in counter mode). */
    public int liveWorkerThreads() {
        return realThreads ? executor.getPoolSize() : 0;
    }

    public boolean isRealThreads() {
        return realThreads;
    }

    public void shutdown() {
        releaseAll();
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private static void parkUntilReleased() {
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static ThreadFactory workerFactory(String roomId) {
        AtomicInteger seq = new AtomicInteger();
        ThreadGroup group = new ThreadGroup("sim-room-" + roomId);
        return runnable -> {
            Thread t = new Thread(group, runnable, "sim-room-" + roomId + "-worker-" + seq.incrementAndGet(),
                    WORKER_STACK_BYTES);
            t.setDaemon(true);
            return t;
        };
    }

    @Override
    public String toString() {
        return "ThreadLeakSimulator[room=" + roomId + ", leaked=" + leakedCount() + "]";
    }
}
