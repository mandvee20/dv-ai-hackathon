package com.example.roomsimulator.controller;

import com.example.roomsimulator.dto.CommandRequest;
import com.example.roomsimulator.dto.DeviceResponse;
import com.example.roomsimulator.service.CommandDispatcher;
import com.example.roomsimulator.service.DeviceSimulationService;
import com.example.roomsimulator.service.HealthSimulationService;
import com.example.roomsimulator.service.RoomSimulationService;
import com.example.roomsimulator.simulator.RoomRuntime;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class RoomController {

    private final RoomSimulationService rooms;
    private final DeviceSimulationService devices;
    private final HealthSimulationService health;
    private final CommandDispatcher dispatcher;

    public RoomController(RoomSimulationService rooms, DeviceSimulationService devices,
                          HealthSimulationService health, CommandDispatcher dispatcher) {
        this.rooms = rooms;
        this.devices = devices;
        this.health = health;
        this.dispatcher = dispatcher;
    }

    /** Same JSON contract as the MQTT command topic; returns what would be published on the response topic. */
    @PostMapping("/commands")
    public Object command(@Valid @RequestBody CommandRequest request) {
        return dispatcher.dispatch(request);
    }

    @GetMapping("/rooms")
    public List<Map<String, Object>> list() {
        return rooms.rooms().stream().map(this::summary).toList();
    }

    @GetMapping("/rooms/{roomId}")
    public Map<String, Object> room(@PathVariable String roomId) {
        RoomRuntime room = rooms.room(roomId);
        Map<String, Object> view = summary(room);
        view.put("devices", room.config().devices());
        view.put("failureSimulation", room.failureSimulation());
        return view;
    }

    @PostMapping("/rooms/{roomId}/devices/{deviceId}/operations/{operation}")
    public DeviceResponse execute(@PathVariable String roomId, @PathVariable String deviceId,
                                  @PathVariable String operation,
                                  @RequestBody(required = false) Map<String, Object> parameters) {
        return devices.execute(new CommandRequest("EXECUTE", roomId, deviceId, null, operation, parameters,
                "REST-" + UUID.randomUUID().toString().substring(0, 8)));
    }

    private Map<String, Object> summary(RoomRuntime room) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("roomId", room.roomId());
        m.put("dvcIp", room.config().dvc().ip());
        m.put("dvcStatus", health.snapshot(room, null).dvc().status());
        m.put("threadCount", room.threadCount());
        m.put("memoryUsagePercent", room.memory().usagePercent());
        m.put("deviceCount", room.config().devices().size());
        m.put("operations", room.operationCount());
        m.put("failures", room.failureCount());
        return m;
    }
}
