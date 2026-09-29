package com.example.roomsimulator.exception;

import org.springframework.http.HttpStatus;

public class SimulatorException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public SimulatorException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static SimulatorException roomNotFound(String roomId) {
        return new SimulatorException(HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND", "Room " + roomId + " is not configured");
    }

    public static SimulatorException badRequest(String message) {
        return new SimulatorException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
