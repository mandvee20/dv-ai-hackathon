package com.example.roomsimulator.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * The single command contract used on room/simulator/command and on POST /api/v1/commands.
 *
 * command: EXECUTE, CHECK_STATE, GC, REDUCE_THREADS or RESET.
 * deviceId / deviceType / operation / parameters are only used by EXECUTE.
 */
public record CommandRequest(
        @NotBlank String command,
        String roomId,
        String deviceId,
        String deviceType,
        String operation,
        Map<String, Object> parameters,
        String requestId) {

    public CommandRequest {
        parameters = parameters == null ? Map.of() : parameters;
    }
}
