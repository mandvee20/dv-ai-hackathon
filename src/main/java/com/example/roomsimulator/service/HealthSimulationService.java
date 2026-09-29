package com.example.roomsimulator.service;

import com.example.roomsimulator.dto.HealthResponse;
import com.example.roomsimulator.simulator.RoomRuntime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/** Answers CHECK_STATE / GET health with the DVC's thread and memory picture. */
@Service
public class HealthSimulationService {

    private final RoomSimulationService rooms;

    public HealthSimulationService(RoomSimulationService rooms) {
        this.rooms = rooms;
    }

    public HealthResponse check(String roomId, String requestId) {
        RoomRuntime room = rooms.room(roomId);
        return snapshot(room, requestId);
    }

    public HealthResponse snapshot(RoomRuntime room, String requestId) {
        var health = room.health();
        var memory = room.memory();
        String base = "/api/v1/rooms/" + room.roomId() + "/actions/";

        List<HealthResponse.Issue> issues = new ArrayList<>();
        if (room.threadAlert()) {
            issues.add(new HealthResponse.Issue("HIGH_THREAD_COUNT",
                    "Thread count " + room.threadCount() + " is above configured threshold " + health.threadThreshold() + ".",
                    "REDUCE_THREADS", base + "reduce-threads"));
        }
        if (memory.leakDetected()) {
            issues.add(new HealthResponse.Issue("MEMORY_LEAK", "Memory usage is continuously increasing.",
                    "GC", base + "gc"));
        }
        if (room.memoryAlert()) {
            issues.add(new HealthResponse.Issue("HIGH_MEMORY_USAGE",
                    "Memory usage " + memory.usagePercent() + "% is above threshold " + health.memoryThresholdPercent() + "%.",
                    "GC", base + "gc"));
        }

        HealthResponse.Memory mem = new HealthResponse.Memory(memory.usedMemoryMb(), memory.totalMemoryMb(),
                memory.usagePercent(), health.memoryThresholdPercent(), room.memoryAlert(), memory.leakDetected());
        HealthResponse.Dvc dvc = new HealthResponse.Dvc(room.config().dvc().ip(), room.config().dvc().port(),
                issues.isEmpty() ? "HEALTHY" : "DEGRADED", room.threadCount(), health.threadThreshold(),
                room.threadAlert(), mem);
        return new HealthResponse(requestId, room.roomId(), dvc, issues, rooms.timestamp());
    }
}
