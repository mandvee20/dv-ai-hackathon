package com.example.roomsimulator.service;

import com.example.roomsimulator.dto.CommandRequest;
import com.example.roomsimulator.dto.DeviceResponse;
import com.example.roomsimulator.exception.SimulatorException;
import com.example.roomsimulator.model.DeviceConfig;
import com.example.roomsimulator.model.OperationConfig;
import com.example.roomsimulator.simulator.DeviceSimulator;
import com.example.roomsimulator.simulator.FailureSimulator;
import com.example.roomsimulator.simulator.RoomRuntime;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/** roomId -> deviceId -> deviceType -> operation -> configured behaviour -> response. */
@Service
public class DeviceSimulationService {

    private static final Logger log = LoggerFactory.getLogger(DeviceSimulationService.class);

    private final RoomSimulationService rooms;
    private final DeviceSimulator deviceSimulator;
    private final FailureSimulator failureSimulator;

    public DeviceSimulationService(RoomSimulationService rooms, DeviceSimulator deviceSimulator,
                                   FailureSimulator failureSimulator) {
        this.rooms = rooms;
        this.deviceSimulator = deviceSimulator;
        this.failureSimulator = failureSimulator;
    }

    public DeviceResponse execute(CommandRequest request) {
        RoomRuntime room = rooms.room(request.roomId());
        if (request.deviceId() == null || request.deviceId().isBlank()) {
            throw SimulatorException.badRequest("deviceId is required for EXECUTE");
        }
        if (request.operation() == null || request.operation().isBlank()) {
            throw SimulatorException.badRequest("operation is required for EXECUTE");
        }
        DeviceConfig device = room.config().device(request.deviceId())
                .orElseThrow(() -> new SimulatorException(HttpStatus.NOT_FOUND, "DEVICE_NOT_FOUND",
                        "Device " + request.deviceId() + " is not configured in room " + room.roomId()));
        if (request.deviceType() != null && !request.deviceType().equalsIgnoreCase(device.deviceType())) {
            throw new SimulatorException(HttpStatus.BAD_REQUEST, "DEVICE_TYPE_MISMATCH",
                    "Device " + device.deviceId() + " in room " + room.roomId() + " is a " + device.deviceType()
                            + ", not a " + request.deviceType());
        }
        String operation = request.operation().toUpperCase(Locale.ROOT);
        OperationConfig behaviour = device.operation(operation)
                .orElseThrow(() -> new SimulatorException(HttpStatus.BAD_REQUEST, "UNSUPPORTED_OPERATION",
                        device.deviceType() + " " + device.deviceId() + " in room " + room.roomId()
                                + " does not support " + operation + "; configured: " + device.operations().keySet()));

        DeviceSimulator.Outcome outcome = deviceSimulator.execute(room, device, operation, behaviour,
                request.parameters());
        room.recordOperation(!outcome.success());
        if (!outcome.success()) {
            failureSimulator.onDeviceFailure(room, outcome.error().type());
        }
        log.debug("Room {} device {} {} {} -> {}", room.roomId(), device.deviceId(), device.deviceType(), operation,
                outcome.success() ? "SUCCESS" : outcome.error().type());

        return new DeviceResponse(request.requestId(), room.roomId(), device.deviceId(), device.deviceType(),
                device.protocol(), operation, request.parameters(), outcome.success() ? "SUCCESS" : "FAILURE",
                outcome.message(), outcome.error(), outcome.durationMs(), rooms.timestamp());
    }
}
