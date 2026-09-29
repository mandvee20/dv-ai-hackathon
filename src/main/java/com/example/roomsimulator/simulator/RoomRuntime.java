package com.example.roomsimulator.simulator;

import com.example.roomsimulator.model.FailureSimulationConfig;
import com.example.roomsimulator.model.HealthConfig;
import com.example.roomsimulator.model.RoomConfig;
import java.util.concurrent.atomic.AtomicLong;

/** Live, mutable state of one room, built from its immutable {@link RoomConfig}. */
public class RoomRuntime {

    private final RoomConfig config;
    private final FailureSimulationConfig failureSimulation;
    private final ThreadLeakSimulator threads;
    private final MemoryLeakSimulator memory;
    private final AtomicLong operations = new AtomicLong();
    private final AtomicLong failures = new AtomicLong();

    public RoomRuntime(RoomConfig config, FailureSimulationConfig globalFailureSimulation) {
        this.config = config;
        this.failureSimulation = config.failureSimulation() != null ? config.failureSimulation() : globalFailureSimulation;
        HealthConfig health = config.dvc().health();

        int initialLeaked = Math.max(0, health.threadCount() - health.baselineThreadCount());
        int maxThreads = Math.max(failureSimulation.threadLeak().maxThreads(), health.threadCount());
        this.threads = new ThreadLeakSimulator(config.roomId(), maxThreads,
                failureSimulation.threadLeak().realThreads());
        this.threads.add(initialLeaked, initialLeaked);

        this.memory = new MemoryLeakSimulator(health.totalMemoryMb(), health.baselineMemoryMb(),
                health.initialUsedMemoryMb(), health.memoryLeak());
    }

    public String roomId() {
        return config.roomId();
    }

    public RoomConfig config() {
        return config;
    }

    public HealthConfig health() {
        return config.dvc().health();
    }

    public FailureSimulationConfig failureSimulation() {
        return failureSimulation;
    }

    public ThreadLeakSimulator threads() {
        return threads;
    }

    public MemoryLeakSimulator memory() {
        return memory;
    }

    public int threadCount() {
        return health().baselineThreadCount() + threads.leakedCount();
    }

    /** Leaked threads allowed on top of the baseline, derived from the configured max thread count. */
    public int leakCap() {
        int max = Math.max(failureSimulation.threadLeak().maxThreads(), health().threadCount());
        return Math.max(0, max - health().baselineThreadCount());
    }

    public boolean threadAlert() {
        return threadCount() > health().threadThreshold();
    }

    public boolean memoryAlert() {
        return memory.usagePercent() > health().memoryThresholdPercent();
    }

    public boolean isDegraded() {
        return threadAlert() || memoryAlert() || memory.leakDetected();
    }

    public void recordOperation(boolean failed) {
        operations.incrementAndGet();
        if (failed) {
            failures.incrementAndGet();
        }
    }

    public long operationCount() {
        return operations.get();
    }

    public long failureCount() {
        return failures.get();
    }

    public void shutdown() {
        threads.shutdown();
    }
}
