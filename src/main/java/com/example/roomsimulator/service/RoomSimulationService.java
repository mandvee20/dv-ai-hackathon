package com.example.roomsimulator.service;

import com.example.roomsimulator.config.SimulatorConfigLoader;
import com.example.roomsimulator.exception.SimulatorException;
import com.example.roomsimulator.model.RoomConfig;
import com.example.roomsimulator.model.SimulatorDefinition;
import com.example.roomsimulator.simulator.RoomRuntime;
import jakarta.annotation.PreDestroy;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Holds the loaded JSON definition and the live runtime state of every room. */
@Service
public class RoomSimulationService {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private final SimulatorConfigLoader loader;
    private volatile SimulatorDefinition definition;
    private volatile Map<String, RoomRuntime> rooms = Map.of();

    public RoomSimulationService(SimulatorConfigLoader loader) {
        this.loader = loader;
        apply(loader.load());
    }

    public SimulatorDefinition definition() {
        return definition;
    }

    public Collection<RoomRuntime> rooms() {
        return rooms.values();
    }

    public RoomRuntime room(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            throw SimulatorException.badRequest("roomId is required");
        }
        RoomRuntime room = rooms.get(roomId);
        if (room == null) {
            throw SimulatorException.roomNotFound(roomId);
        }
        return room;
    }

    /** Re-reads the JSON file and rebuilds every room from scratch. MQTT settings need a restart. */
    public synchronized List<String> reload() {
        apply(loader.load());
        return new ArrayList<>(rooms.keySet());
    }

    /** Restores one room to the state its JSON describes. */
    public synchronized RoomRuntime resetRoom(String roomId) {
        RoomRuntime old = room(roomId);
        RoomRuntime fresh = new RoomRuntime(old.config(), definition.failureSimulation());
        Map<String, RoomRuntime> copy = new LinkedHashMap<>(rooms);
        copy.put(roomId, fresh);
        rooms = copy;
        old.shutdown();
        return fresh;
    }

    public String timestamp() {
        return OffsetDateTime.now(ZoneId.of(definition.timezone())).format(TIMESTAMP);
    }

    private synchronized void apply(SimulatorDefinition newDefinition) {
        Map<String, RoomRuntime> built = new LinkedHashMap<>();
        for (RoomConfig room : newDefinition.rooms()) {
            built.put(room.roomId(), new RoomRuntime(room, newDefinition.failureSimulation()));
        }
        Map<String, RoomRuntime> previous = rooms;
        definition = newDefinition;
        rooms = built;
        previous.values().forEach(RoomRuntime::shutdown);
    }

    @PreDestroy
    public void shutdown() {
        rooms.values().forEach(RoomRuntime::shutdown);
    }
}
