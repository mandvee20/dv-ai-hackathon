package com.example.roomsimulator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HealthResponse(
        String requestId,
        String roomId,
        Dvc dvc,
        List<Issue> issues,
        String timestamp) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Dvc(
            String ip,
            Integer port,
            String status,
            int threadCount,
            int threadThreshold,
            boolean threadAlert,
            Memory memory) {
    }

    public record Memory(
            int usedMemoryMb,
            int totalMemoryMb,
            int usagePercent,
            int thresholdPercent,
            boolean memoryAlert,
            boolean memoryLeakDetected) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Issue(String type, String message, String suggestedAction, String endpoint) {
    }
}
