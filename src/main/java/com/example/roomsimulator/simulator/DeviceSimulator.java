package com.example.roomsimulator.simulator;

import com.example.roomsimulator.dto.ErrorDetail;
import com.example.roomsimulator.model.DeviceConfig;
import com.example.roomsimulator.model.OperationConfig;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Decides the outcome of one device operation from its configured behaviour. */
@Component
public class DeviceSimulator {

    public record Outcome(boolean success, String message, ErrorDetail error, long durationMs) {
    }

    public Outcome execute(RoomRuntime room, DeviceConfig device, String operation, OperationConfig behaviour,
                           Map<String, Object> parameters) {
        long start = System.currentTimeMillis();
        boolean fails = behaviour.isFailure() && (!behaviour.failOnlyWhenDegraded() || room.isDegraded());
        if (fails || !behaviour.isFailure()) {
            sleep(behaviour.delayMs()); // a timeout only takes long while it is actually timing out
        }
        long duration = System.currentTimeMillis() - start;
        if (!fails) {
            String message = behaviour.isFailure() || behaviour.message() == null
                    ? successMessage(device.deviceType(), operation, parameters)
                    : behaviour.message();
            return new Outcome(true, message, null, duration);
        }
        String type = behaviour.exception() == null ? "DeviceOperationException" : behaviour.exception();
        ErrorDetail error = new ErrorDetail(type,
                behaviour.message() == null ? device.deviceType() + " " + operation + " failed" : behaviour.message(),
                behaviour.description(), behaviour.possibleCauses(), behaviour.recommendedAction());
        return new Outcome(false, null, error, duration);
    }

    static String successMessage(String deviceType, String operation, Map<String, Object> parameters) {
        String op = operation.toUpperCase(Locale.ROOT);
        String base = switch (op) {
            case "ON", "OFF" -> deviceType + " turned " + op + " successfully";
            case "OPEN" -> deviceType + " opened successfully";
            case "CLOSE" -> deviceType + " closed successfully";
            case "STOP" -> deviceType + " stopped successfully";
            default -> deviceType + " " + op + " executed successfully";
        };
        if (parameters == null || parameters.isEmpty()) {
            return base;
        }
        return base + " (" + parameters.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ")) + ")";
    }

    private static void sleep(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
