package com.example.roomsimulator.controller;

import com.example.roomsimulator.dto.HealthResponse;
import com.example.roomsimulator.model.MqttSettings;
import com.example.roomsimulator.model.SimulatorDefinition;
import com.example.roomsimulator.mqtt.MqttConnectionManager;
import com.example.roomsimulator.service.HealthSimulationService;
import com.example.roomsimulator.service.RoomSimulationService;
import com.example.roomsimulator.simulator.RoomRuntime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simulation controls, kept apart from the self-fix actions: these break things on purpose or
 * manage the simulator itself.
 */
@RestController
@RequestMapping("/api/v1/simulation")
public class SimulationController {

    private final RoomSimulationService rooms;
    private final HealthSimulationService health;
    private final MqttConnectionManager mqtt;

    public SimulationController(RoomSimulationService rooms, HealthSimulationService health,
                                MqttConnectionManager mqtt) {
        this.rooms = rooms;
        this.health = health;
        this.mqtt = mqtt;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("mqtt", mqtt.status());
        m.put("rooms", rooms.rooms().size());
        m.put("jvmLiveThreads", Thread.activeCount());
        m.put("timestamp", rooms.timestamp());
        return m;
    }

    @GetMapping("/config")
    public SimulatorDefinition config() {
        SimulatorDefinition d = rooms.definition();
        // never echo the broker password
        var m = d.mqtt();
        var masked = new MqttSettings(m.enabled(), m.brokerHost(), m.brokerPort(),
                m.clientId(), m.username(), m.password() == null ? null : "****", m.commandTopic(), m.responseTopic(),
                m.qos());
        return new SimulatorDefinition(masked, d.timezone(), d.failureSimulation(), d.rooms());
    }

    /** Re-reads simulator-config.json (use a file: path to edit it without a rebuild). */
    @PostMapping("/reload")
    public Map<String, Object> reload() {
        return Map.of("status", "SUCCESS", "rooms", rooms.reload(),
                "note", "Room behaviour reloaded. MQTT connection settings apply after a restart.");
    }

    /** Adds simulator-owned threads to a room's DVC, as if failures had piled up. */
    @PostMapping("/rooms/{roomId}/thread-spike")
    public HealthResponse threadSpike(@PathVariable String roomId, @RequestParam(defaultValue = "100") int threads) {
        RoomRuntime room = rooms.room(roomId);
        room.threads().add(threads, room.leakCap());
        return health.snapshot(room, null);
    }

    /** Leaks simulated memory into a room's DVC. */
    @PostMapping("/rooms/{roomId}/memory-leak")
    public HealthResponse memoryLeak(@PathVariable String roomId, @RequestParam(defaultValue = "500") int mb) {
        RoomRuntime room = rooms.room(roomId);
        room.memory().leak(mb, room.failureSimulation().memoryLeak().maxMemoryMb());
        return health.snapshot(room, null);
    }
}
