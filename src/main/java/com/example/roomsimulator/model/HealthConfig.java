package com.example.roomsimulator.model;

import java.util.Locale;

/**
 * Starting health of a room's DVC. Only threadCount / memoryUsage / memoryLeak are needed;
 * everything else has a sensible default.
 */
public record HealthConfig(
        Integer threadCount,
        Integer baselineThreadCount,
        Integer threadThreshold,
        /** LOW / NORMAL / MEDIUM / HIGH / CRITICAL, used when memoryUsagePercent is absent. */
        String memoryUsage,
        Integer memoryUsagePercent,
        Integer memoryThresholdPercent,
        Integer totalMemoryMb,
        Integer baselineMemoryMb,
        Boolean memoryLeak) {

    public HealthConfig {
        baselineThreadCount = baselineThreadCount == null ? 50 : baselineThreadCount;
        threadCount = threadCount == null ? baselineThreadCount : threadCount;
        threadThreshold = threadThreshold == null ? 500 : threadThreshold;
        memoryThresholdPercent = memoryThresholdPercent == null ? 80 : memoryThresholdPercent;
        totalMemoryMb = totalMemoryMb == null ? 8500 : totalMemoryMb;
        memoryUsagePercent = memoryUsagePercent == null ? percentFor(memoryUsage) : memoryUsagePercent;
        baselineMemoryMb = baselineMemoryMb == null ? totalMemoryMb * 40 / 100 : baselineMemoryMb;
        memoryLeak = memoryLeak != null && memoryLeak;
    }

    public static HealthConfig healthy() {
        return new HealthConfig(null, null, null, "NORMAL", null, null, null, null, false);
    }

    public int initialUsedMemoryMb() {
        return totalMemoryMb * memoryUsagePercent / 100;
    }

    private static int percentFor(String label) {
        if (label == null) {
            return 40;
        }
        return switch (label.toUpperCase(Locale.ROOT)) {
            case "LOW" -> 25;
            case "MEDIUM" -> 65;
            case "HIGH" -> 92;
            case "CRITICAL" -> 97;
            default -> 40;
        };
    }
}
