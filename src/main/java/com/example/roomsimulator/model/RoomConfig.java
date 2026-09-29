package com.example.roomsimulator.model;

import java.util.List;
import java.util.Optional;

public record RoomConfig(
        String roomId,
        DvcConfig dvc,
        /** Optional per-room override of the global failureSimulation block. */
        FailureSimulationConfig failureSimulation,
        List<DeviceConfig> devices) {

    public RoomConfig {
        dvc = dvc == null ? new DvcConfig(null, null, null) : dvc;
        devices = devices == null ? List.of() : List.copyOf(devices);
    }

    public Optional<DeviceConfig> device(String deviceId) {
        return devices.stream().filter(d -> d.deviceId().equals(deviceId)).findFirst();
    }
}
