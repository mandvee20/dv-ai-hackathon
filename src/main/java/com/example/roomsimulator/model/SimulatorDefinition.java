package com.example.roomsimulator.model;

import java.util.List;

/**
 * Root of simulator-config.json. Everything about rooms, devices and their behaviour
 * lives here, so adding a room never needs a Java change.
 */
public record SimulatorDefinition(
        MqttSettings mqtt,
        String timezone,
        FailureSimulationConfig failureSimulation,
        List<RoomConfig> rooms) {

    public SimulatorDefinition {
        mqtt = mqtt == null ? new MqttSettings(null, null, null, null, null, null, null, null, null) : mqtt;
        timezone = timezone == null || timezone.isBlank() ? "Asia/Kolkata" : timezone;
        failureSimulation = failureSimulation == null ? FailureSimulationConfig.disabled() : failureSimulation;
        rooms = rooms == null ? List.of() : List.copyOf(rooms);
    }
}
