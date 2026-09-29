package com.example.roomsimulator.model;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public record DeviceConfig(
        String deviceId,
        String deviceType,
        String protocol,
        String name,
        Map<String, OperationConfig> operations) {

    public DeviceConfig {
        protocol = protocol == null ? "IP" : protocol;
        operations = operations == null ? Map.of() : Map.copyOf(operations);
    }

    public Optional<OperationConfig> operation(String operation) {
        if (operation == null) {
            return Optional.empty();
        }
        OperationConfig exact = operations.get(operation);
        if (exact != null) {
            return Optional.of(exact);
        }
        return operations.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(operation.toUpperCase(Locale.ROOT)))
                .map(Map.Entry::getValue)
                .findFirst();
    }
}
