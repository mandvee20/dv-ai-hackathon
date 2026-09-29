package com.example.roomsimulator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String requestId, String roomId, String status, String code, String message,
                            String timestamp) {

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(null, null, "ERROR", code, message, OffsetDateTime.now().toString());
    }

    public ErrorResponse withRequest(String requestId, String roomId) {
        return new ErrorResponse(requestId, roomId, status, code, message, timestamp);
    }
}
