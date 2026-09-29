package com.example.roomsimulator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record DeviceResponse(
        String requestId,
        String roomId,
        String deviceId,
        String deviceType,
        String protocol,
        String operation,
        Map<String, Object> parameters,
        String status,
        String message,
        ErrorDetail error,
        Long durationMs,
        String timestamp) {
}
