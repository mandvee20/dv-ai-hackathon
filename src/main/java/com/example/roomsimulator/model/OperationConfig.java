package com.example.roomsimulator.model;

import java.util.List;

/**
 * Configured behaviour of one device operation.
 *
 * result: SUCCESS or FAILURE.
 * failOnlyWhenDegraded: when true the failure only happens while the room's DVC is unhealthy,
 * so the operation starts working again after the self-fix actions bring the DVC back.
 */
public record OperationConfig(
        String result,
        String exception,
        String message,
        String description,
        List<String> possibleCauses,
        List<String> recommendedAction,
        Long delayMs,
        Boolean failOnlyWhenDegraded) {

    public OperationConfig {
        result = result == null ? "SUCCESS" : result.toUpperCase();
        possibleCauses = possibleCauses == null ? List.of() : List.copyOf(possibleCauses);
        recommendedAction = recommendedAction == null ? List.of() : List.copyOf(recommendedAction);
        delayMs = delayMs == null ? 0L : delayMs;
        failOnlyWhenDegraded = failOnlyWhenDegraded != null && failOnlyWhenDegraded;
    }

    public boolean isFailure() {
        return "FAILURE".equals(result) || "FAILED".equals(result);
    }
}
