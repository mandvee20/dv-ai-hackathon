package com.example.roomsimulator.config;

import com.example.roomsimulator.exception.SimulatorException;
import com.example.roomsimulator.model.MqttSettings;
import com.example.roomsimulator.model.RoomConfig;
import com.example.roomsimulator.model.SimulatorDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Reads and validates simulator-config.json. */
@Component
public class SimulatorConfigLoader {

    private static final Logger log = LoggerFactory.getLogger(SimulatorConfigLoader.class);

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final SimulatorProperties properties;

    public SimulatorConfigLoader(ResourceLoader resourceLoader, ObjectMapper objectMapper,
                                 SimulatorProperties properties) {
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public SimulatorDefinition load() {
        Resource resource = resourceLoader.getResource(properties.configFile());
        try (InputStream in = resource.getInputStream()) {
            SimulatorDefinition definition = objectMapper.readValue(in, SimulatorDefinition.class);
            validate(definition);
            definition = applyOverrides(definition);
            log.info("Loaded {} room(s) from {}", definition.rooms().size(), properties.configFile());
            return definition;
        } catch (IOException e) {
            throw new SimulatorException(HttpStatus.INTERNAL_SERVER_ERROR, "CONFIG_ERROR",
                    "Cannot read " + properties.configFile() + ": " + e.getMessage());
        }
    }

    private SimulatorDefinition applyOverrides(SimulatorDefinition definition) {
        SimulatorProperties.MqttOverride override = properties.mqtt();
        MqttSettings mqtt = definition.mqtt().withBroker(override.brokerHost(), override.brokerPort());
        if (override.enabled() != null) {
            mqtt = mqtt.withEnabled(override.enabled());
        }
        return new SimulatorDefinition(mqtt, definition.timezone(), definition.failureSimulation(), definition.rooms());
    }

    private void validate(SimulatorDefinition definition) {
        Set<String> roomIds = new HashSet<>();
        for (RoomConfig room : definition.rooms()) {
            if (room.roomId() == null || room.roomId().isBlank()) {
                throw configError("Every room needs a roomId");
            }
            if (!roomIds.add(room.roomId())) {
                throw configError("Duplicate roomId " + room.roomId());
            }
            Set<String> deviceIds = new HashSet<>();
            room.devices().forEach(device -> {
                if (device.deviceId() == null || device.deviceType() == null) {
                    throw configError("Room " + room.roomId() + " has a device without deviceId or deviceType");
                }
                if (!deviceIds.add(device.deviceId())) {
                    throw configError("Room " + room.roomId() + " has duplicate deviceId " + device.deviceId());
                }
            });
        }
    }

    private SimulatorException configError(String message) {
        return new SimulatorException(HttpStatus.INTERNAL_SERVER_ERROR, "CONFIG_ERROR", message);
    }
}
