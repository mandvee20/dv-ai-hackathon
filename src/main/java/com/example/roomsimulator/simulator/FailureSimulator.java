package com.example.roomsimulator.simulator;

import com.example.roomsimulator.model.FailureSimulationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Turns device failures into DVC load (threads and memory), per the failureSimulation block. */
@Component
public class FailureSimulator {

    private static final Logger log = LoggerFactory.getLogger(FailureSimulator.class);

    public void onDeviceFailure(RoomRuntime room, String exceptionType) {
        FailureSimulationConfig sim = room.failureSimulation();
        if (!sim.triggeredBy(exceptionType)) {
            return;
        }
        int threadsAdded = 0;
        int memoryAdded = 0;
        if (sim.threadLeak().enabled()) {
            threadsAdded = room.threads().add(sim.threadLeak().threadsPerFailure(), room.leakCap());
        }
        if (sim.memoryLeak().enabled()) {
            memoryAdded = room.memory().leak(sim.memoryLeak().memoryPerFailureMb(), sim.memoryLeak().maxMemoryMb());
        }
        log.info("Room {} {} -> +{} threads (now {}), +{} MB (now {} MB, {}%)", room.roomId(), exceptionType,
                threadsAdded, room.threadCount(), memoryAdded, room.memory().usedMemoryMb(),
                room.memory().usagePercent());
    }
}
