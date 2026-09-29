package com.example.roomsimulator.service;

import com.example.roomsimulator.dto.HealthResponse;
import com.example.roomsimulator.dto.SelfFixResponse;
import com.example.roomsimulator.simulator.RoomRuntime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Remediation actions an AI self-fix agent can call. */
@Service
public class SelfFixService {

    private static final Logger log = LoggerFactory.getLogger(SelfFixService.class);
    private static final long MB = 1024 * 1024;

    private final RoomSimulationService rooms;
    private final HealthSimulationService health;

    public SelfFixService(RoomSimulationService rooms, HealthSimulationService health) {
        this.rooms = rooms;
        this.health = health;
    }

    /** Releases the room's simulated leaked memory and asks the real JVM for a GC. */
    public SelfFixResponse garbageCollect(String roomId, String requestId) {
        RoomRuntime room = rooms.room(roomId);
        int before = room.memory().usedMemoryMb();
        int beforePercent = room.memory().usagePercent();
        room.memory().collect();

        Runtime rt = Runtime.getRuntime();
        long heapBefore = (rt.totalMemory() - rt.freeMemory()) / MB;
        System.gc(); // a request, not a guarantee; fine for a simulator
        long heapAfter = (rt.totalMemory() - rt.freeMemory()) / MB;

        Map<String, Object> jvm = new LinkedHashMap<>();
        jvm.put("heapUsedMbBefore", heapBefore);
        jvm.put("heapUsedMbAfter", heapAfter);
        jvm.put("note", "System.gc() is a request; simulated DVC memory above is what the room reports.");

        log.info("Room {} GC: {} MB -> {} MB", roomId, before, room.memory().usedMemoryMb());
        return new SelfFixResponse(requestId, roomId, "GARBAGE_COLLECTION", "SUCCESS",
                Map.of("usedMemoryMb", before, "usagePercent", beforePercent),
                Map.of("usedMemoryMb", room.memory().usedMemoryMb(), "usagePercent", room.memory().usagePercent()),
                null, null, null, jvm, "Garbage collection requested.", rooms.timestamp());
    }

    /** Cancels the simulator-owned leaked worker threads of the room. */
    public SelfFixResponse reduceThreads(String roomId, String requestId) {
        RoomRuntime room = rooms.room(roomId);
        int before = room.threadCount();
        int released = room.threads().releaseAll();
        int after = room.threadCount();
        awaitWorkersGone(room);

        Map<String, Object> jvm = new LinkedHashMap<>();
        jvm.put("realThreads", room.threads().isRealThreads());
        jvm.put("jvmLiveThreads", Thread.activeCount());

        log.info("Room {} reduce-threads: {} -> {} ({} released)", roomId, before, after, released);
        return new SelfFixResponse(requestId, roomId, "REDUCE_THREADS", "SUCCESS", null, null, before, after,
                released, jvm, "Released " + released + " simulator-owned worker threads.", rooms.timestamp());
    }

    private static void awaitWorkersGone(RoomRuntime room) {
        long deadline = System.currentTimeMillis() + 2000;
        while (room.threads().liveWorkerThreads() > 0 && System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /** Puts the room back to exactly what the JSON describes (useful between demo runs). */
    public SelfFixResponse reset(String roomId, String requestId) {
        HealthResponse before = health.check(roomId, null);
        RoomRuntime fresh = rooms.resetRoom(roomId);
        HealthResponse after = health.snapshot(fresh, null);
        return new SelfFixResponse(requestId, roomId, "RESET", "SUCCESS", summary(before), summary(after),
                null, null, null, null, "Room restored to its configured initial state.", rooms.timestamp());
    }

    private static Map<String, Object> summary(HealthResponse h) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("status", h.dvc().status());
        m.put("threadCount", h.dvc().threadCount());
        m.put("memoryUsagePercent", h.dvc().memory().usagePercent());
        m.put("memoryLeakDetected", h.dvc().memory().memoryLeakDetected());
        return m;
    }
}
