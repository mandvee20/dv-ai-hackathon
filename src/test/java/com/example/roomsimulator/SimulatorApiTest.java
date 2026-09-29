package com.example.roomsimulator;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest(properties = "simulator.mqtt.enabled=false")
@AutoConfigureMockMvc
class SimulatorApiTest {

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void resetRooms() throws Exception {
        for (String room : new String[] {"420", "520"}) {
            mvc.perform(post("/api/v1/rooms/" + room + "/actions/reset")).andExpect(status().isOk());
        }
    }

    private ResultActions command(String json) throws Exception {
        return mvc.perform(post("/api/v1/commands").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private static String execute(String room, String device, String type, String op, String requestId) {
        return """
                {"command":"EXECUTE","roomId":"%s","deviceId":"%s","deviceType":"%s","operation":"%s","requestId":"%s"}
                """.formatted(room, device, type, op, requestId);
    }

    @Test
    void room101TvOnSucceeds() throws Exception {
        command(execute("101", "1", "TV", "ON", "REQ-10001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("REQ-10001"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("TV turned ON successfully"))
                .andExpect(jsonPath("$.timestamp").value(containsString("+05:30")))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void genericOperationsEchoParameters() throws Exception {
        command("""
                {"command":"EXECUTE","roomId":"101","deviceId":"4","deviceType":"AC","operation":"SET_TEMPERATURE",
                 "parameters":{"temperature":22},"requestId":"REQ-AC"}
                """)
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.parameters.temperature").value(22))
                .andExpect(jsonPath("$.message").value("AC SET_TEMPERATURE executed successfully (temperature=22)"));
    }

    @Test
    void room320FailuresCarryConfiguredErrors() throws Exception {
        command(execute("320", "1", "TV", "ON", "REQ-20001"))
                .andExpect(jsonPath("$.status").value("FAILURE"))
                .andExpect(jsonPath("$.error.type").value("ConnectException"))
                .andExpect(jsonPath("$.error.message").value("Connection refused"));
        command(execute("320", "3", "CURTAIN", "OPEN", "REQ-20002"))
                .andExpect(jsonPath("$.error.type").value("SocketException"))
                .andExpect(jsonPath("$.error.message").value("Network is unreachable"));
        command(execute("320", "4", "LIGHT", "ON", "REQ-20003"))
                .andExpect(jsonPath("$.protocol").value("KNX"))
                .andExpect(jsonPath("$.error.type").value("KnxSecureException"))
                .andExpect(jsonPath("$.error.possibleCauses").value(hasItem("Corrupted KNX keyring")))
                .andExpect(jsonPath("$.error.recommendedAction").value(hasItem("Verify configured credentials")));
    }

    @Test
    void badLookupsAreRejected() throws Exception {
        command(execute("999", "1", "TV", "ON", "R1")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ROOM_NOT_FOUND"));
        command(execute("101", "9", "TV", "ON", "R2")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DEVICE_NOT_FOUND"));
        command(execute("101", "1", "LIGHT", "ON", "R3")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DEVICE_TYPE_MISMATCH"));
        command(execute("101", "3", "CURTAIN", "FLY", "R4")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_OPERATION"));
        command("{\"command\":\"DANCE\",\"roomId\":\"101\"}").andExpect(status().isBadRequest());
    }

    @Test
    void room420SelfHealingFlow() throws Exception {
        command("{\"command\":\"CHECK_STATE\",\"roomId\":\"420\",\"requestId\":\"HEALTH-420-001\"}")
                .andExpect(jsonPath("$.requestId").value("HEALTH-420-001"))
                .andExpect(jsonPath("$.dvc.ip").value("10.10.10.12"))
                .andExpect(jsonPath("$.dvc.status").value("DEGRADED"))
                .andExpect(jsonPath("$.dvc.threadCount").value(700))
                .andExpect(jsonPath("$.dvc.threadThreshold").value(500))
                .andExpect(jsonPath("$.dvc.threadAlert").value(true))
                .andExpect(jsonPath("$.dvc.memory.usagePercent").value(92))
                .andExpect(jsonPath("$.dvc.memory.memoryAlert").value(true))
                .andExpect(jsonPath("$.dvc.memory.memoryLeakDetected").value(true))
                .andExpect(jsonPath("$.issues[*].type").value(hasItem("HIGH_THREAD_COUNT")))
                .andExpect(jsonPath("$.issues[*].type").value(hasItem("MEMORY_LEAK")));

        command(execute("420", "1", "TV", "ON", "REQ-42001"))
                .andExpect(jsonPath("$.status").value("FAILURE"))
                .andExpect(jsonPath("$.error.type").value("SocketTimeoutException"));

        mvc.perform(post("/api/v1/rooms/420/actions/gc"))
                .andExpect(jsonPath("$.action").value("GARBAGE_COLLECTION"))
                .andExpect(jsonPath("$.before.usedMemoryMb").value(7870))
                .andExpect(jsonPath("$.after.usedMemoryMb").value(5200));

        mvc.perform(post("/api/v1/rooms/420/actions/reduce-threads"))
                .andExpect(jsonPath("$.action").value("REDUCE_THREADS"))
                .andExpect(jsonPath("$.beforeThreadCount").value(700))
                .andExpect(jsonPath("$.afterThreadCount").value(80))
                .andExpect(jsonPath("$.threadsReleased").value(620));

        mvc.perform(get("/api/v1/rooms/420/health"))
                .andExpect(jsonPath("$.dvc.status").value("HEALTHY"))
                .andExpect(jsonPath("$.issues").isEmpty());

        command(execute("420", "1", "TV", "ON", "REQ-42002"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        mvc.perform(post("/api/v1/rooms/420/actions/reset"))
                .andExpect(jsonPath("$.after.status").value("DEGRADED"))
                .andExpect(jsonPath("$.after.threadCount").value(700));
    }

    @Test
    void room520LeaksUntilDegraded() throws Exception {
        mvc.perform(get("/api/v1/rooms/520/health")).andExpect(jsonPath("$.dvc.status").value("HEALTHY"));
        for (int i = 0; i < 10; i++) {
            command(execute("520", "1", "TV", "ON", "REQ-520-" + i)).andExpect(jsonPath("$.status").value("FAILURE"));
        }
        mvc.perform(get("/api/v1/rooms/520/health"))
                .andExpect(jsonPath("$.dvc.status").value("DEGRADED"))
                .andExpect(jsonPath("$.dvc.threadCount").value(550))
                .andExpect(jsonPath("$.dvc.memory.usedMemoryMb").value(7400))
                .andExpect(jsonPath("$.dvc.memory.memoryAlert").value(true));
    }

    @Test
    void restDeviceEndpointAndRoomList() throws Exception {
        mvc.perform(post("/api/v1/rooms/101/devices/2/operations/dim")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"level\":40}"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.operation").value("DIM"));
        mvc.perform(get("/api/v1/rooms"))
                .andExpect(jsonPath("$[*].roomId").value(hasItem("420")));
        mvc.perform(get("/api/v1/simulation/status"))
                .andExpect(jsonPath("$.mqtt.enabled").value(false));
    }
}
