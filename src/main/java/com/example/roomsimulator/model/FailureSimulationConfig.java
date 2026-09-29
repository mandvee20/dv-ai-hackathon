package com.example.roomsimulator.model;

import java.util.List;

/**
 * Intentional degradation: every matching device failure adds simulator-owned worker threads
 * and simulated leaked memory to the room's DVC.
 */
public record FailureSimulationConfig(
        Boolean enabled,
        /** Exception types that trigger the leak. Empty means every failure does. */
        List<String> triggerOn,
        ThreadLeak threadLeak,
        MemoryLeak memoryLeak) {

    public FailureSimulationConfig {
        enabled = enabled != null && enabled;
        triggerOn = triggerOn == null ? List.of() : List.copyOf(triggerOn);
        threadLeak = threadLeak == null ? new ThreadLeak(false, null, null, null) : threadLeak;
        memoryLeak = memoryLeak == null ? new MemoryLeak(false, null, null) : memoryLeak;
    }

    public static FailureSimulationConfig disabled() {
        return new FailureSimulationConfig(false, null, null, null);
    }

    public boolean triggeredBy(String exceptionType) {
        return enabled && (triggerOn.isEmpty() || triggerOn.stream().anyMatch(t -> t.equalsIgnoreCase(exceptionType)));
    }

    public record ThreadLeak(Boolean enabled, Integer threadsPerFailure, Integer maxThreads, Boolean realThreads) {
        public ThreadLeak {
            enabled = enabled != null && enabled;
            threadsPerFailure = threadsPerFailure == null ? 50 : threadsPerFailure;
            maxThreads = maxThreads == null ? 700 : maxThreads;
            // Real threads are parked daemon workers owned by the simulator; false keeps only a counter.
            realThreads = realThreads == null ? Boolean.TRUE : realThreads;
        }
    }

    public record MemoryLeak(Boolean enabled, Integer memoryPerFailureMb, Integer maxMemoryMb) {
        public MemoryLeak {
            enabled = enabled != null && enabled;
            memoryPerFailureMb = memoryPerFailureMb == null ? 50 : memoryPerFailureMb;
            maxMemoryMb = maxMemoryMb == null ? 8000 : maxMemoryMb;
        }
    }
}
