package com.example.roomsimulator.service;

import com.example.roomsimulator.dto.CommandRequest;
import com.example.roomsimulator.exception.SimulatorException;
import java.util.Locale;
import org.springframework.stereotype.Service;

/** Routes a command from the common topic (or POST /api/v1/commands) to the right service. */
@Service
public class CommandDispatcher {

    private final DeviceSimulationService devices;
    private final HealthSimulationService health;
    private final SelfFixService selfFix;

    public CommandDispatcher(DeviceSimulationService devices, HealthSimulationService health, SelfFixService selfFix) {
        this.devices = devices;
        this.health = health;
        this.selfFix = selfFix;
    }

    public Object dispatch(CommandRequest request) {
        String command = request.command() == null ? "" : request.command().toUpperCase(Locale.ROOT).replace('-', '_');
        return switch (command) {
            case "EXECUTE" -> devices.execute(request);
            case "CHECK_STATE", "HEALTH" -> health.check(request.roomId(), request.requestId());
            case "GC", "GARBAGE_COLLECTION" -> selfFix.garbageCollect(request.roomId(), request.requestId());
            case "REDUCE_THREADS" -> selfFix.reduceThreads(request.roomId(), request.requestId());
            case "RESET" -> selfFix.reset(request.roomId(), request.requestId());
            default -> throw SimulatorException.badRequest("Unknown command '" + request.command()
                    + "'. Use EXECUTE, CHECK_STATE, GC, REDUCE_THREADS or RESET.");
        };
    }
}
