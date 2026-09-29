package com.example.roomsimulator.controller;

import com.example.roomsimulator.dto.HealthResponse;
import com.example.roomsimulator.dto.SelfFixResponse;
import com.example.roomsimulator.service.HealthSimulationService;
import com.example.roomsimulator.service.SelfFixService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** DVC health and the self-fix actions. */
@RestController
@RequestMapping("/api/v1/rooms/{roomId}")
public class HealthController {

    private final HealthSimulationService health;
    private final SelfFixService selfFix;

    public HealthController(HealthSimulationService health, SelfFixService selfFix) {
        this.health = health;
        this.selfFix = selfFix;
    }

    @GetMapping("/health")
    public HealthResponse health(@PathVariable String roomId, @RequestParam(required = false) String requestId) {
        return health.check(roomId, requestId);
    }

    @PostMapping("/actions/gc")
    public SelfFixResponse gc(@PathVariable String roomId, @RequestParam(required = false) String requestId) {
        return selfFix.garbageCollect(roomId, requestId);
    }

    @PostMapping("/actions/reduce-threads")
    public SelfFixResponse reduceThreads(@PathVariable String roomId,
                                         @RequestParam(required = false) String requestId) {
        return selfFix.reduceThreads(roomId, requestId);
    }

    @PostMapping("/actions/reset")
    public SelfFixResponse reset(@PathVariable String roomId, @RequestParam(required = false) String requestId) {
        return selfFix.reset(roomId, requestId);
    }
}
