package com.example.roomsimulator.simulator;

/**
 * Simulated DVC memory for one room. The numbers are simulated (a hackathon laptop cannot hold
 * 8 GB of garbage); only the GC action additionally asks the real JVM to collect.
 */
public class MemoryLeakSimulator {

    private final int totalMemoryMb;
    private final int baselineMemoryMb;
    private int usedMemoryMb;
    private boolean leakDetected;

    public MemoryLeakSimulator(int totalMemoryMb, int baselineMemoryMb, int initialUsedMb, boolean leakDetected) {
        this.totalMemoryMb = totalMemoryMb;
        this.baselineMemoryMb = Math.min(baselineMemoryMb, totalMemoryMb);
        this.usedMemoryMb = Math.min(initialUsedMb, totalMemoryMb);
        this.leakDetected = leakDetected;
    }

    /** Leaks {@code mb} more, never beyond {@code maxMemoryMb}. Returns the MB actually added. */
    public synchronized int leak(int mb, int maxMemoryMb) {
        int cap = Math.min(maxMemoryMb, totalMemoryMb);
        int added = Math.max(0, Math.min(mb, cap - usedMemoryMb));
        usedMemoryMb += added;
        leakDetected = true;
        return added;
    }

    /** Releases everything above the baseline and clears the leak flag. Returns the MB freed. */
    public synchronized int collect() {
        int freed = Math.max(0, usedMemoryMb - baselineMemoryMb);
        usedMemoryMb -= freed;
        leakDetected = false;
        return freed;
    }

    public synchronized int usedMemoryMb() {
        return usedMemoryMb;
    }

    public synchronized boolean leakDetected() {
        return leakDetected;
    }

    public int totalMemoryMb() {
        return totalMemoryMb;
    }

    public int baselineMemoryMb() {
        return baselineMemoryMb;
    }

    public synchronized int usagePercent() {
        return totalMemoryMb == 0 ? 0 : Math.round(usedMemoryMb * 100f / totalMemoryMb);
    }
}
