package com.example.roomsimulator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SelfFixResponse(
        String requestId,
        String roomId,
        String action,
        String status,
        Map<String, Object> before,
        Map<String, Object> after,
        Integer beforeThreadCount,
        Integer afterThreadCount,
        Integer threadsReleased,
        /** Real JVM numbers, kept apart from the simulated DVC numbers. */
        Map<String, Object> jvm,
        String message,
        String timestamp) {
}
