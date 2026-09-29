# Room Simulator (Spring Boot)

A hotel room / DVC simulator whose behaviour lives entirely in `simulator-config.json`.
External apps (the AI agent, the dashboard, the Jira router) send commands on one MQTT topic or over
REST and get back SUCCESS / FAILURE / health responses exactly as the JSON describes. Adding a room,
device or operation is a JSON edit, not a Java change.

```
            External application / AI agent
                 │ MQTT                 │ REST
                 ▼                      ▼
     room/simulator/command     /api/v1/...
                 │                      │
                 └──────► Simulator engine ◄── simulator-config.json
                                 │
        ┌──────────────┬─────────┴────────┬──────────────┐
     Room 101       Room 320           Room 420        Room 520
     all PASS       network/KNX        DVC degraded    leaks on every
                    failures           (700 threads,   TV timeout
                                        92% memory)
                                 │
                 room/simulator/response
```

## Run it

Requirements: Java 17+, Maven 3.9+.

```bash
mvn spring-boot:run                      # or: mvn package && java -jar target/room-simulator-1.0.0.jar
```

The broker in the JSON is `10.75.0.118:1883`. To use another broker without editing the JSON:

```bash
SIMULATOR_MQTT_BROKER_HOST=localhost mvn spring-boot:run
SIMULATOR_MQTT_ENABLED=false mvn spring-boot:run        # REST only
docker compose up --build                               # Mosquitto + simulator
```

If the broker is unreachable the app still starts, the REST APIs work, and MQTT keeps retrying every 5 s.
`GET /api/v1/simulation/status` shows whether MQTT is connected.

Tests: `mvn test` (8 tests: every room scenario over REST, plus a real MQTT round trip on an embedded broker).

## Command contract (MQTT topic and `POST /api/v1/commands`)

```json
{ "command": "EXECUTE", "roomId": "101", "deviceId": "1", "deviceType": "TV",
  "operation": "ON", "parameters": {}, "requestId": "REQ-10001" }
```

| command | needs | returns |
|---|---|---|
| `EXECUTE` | roomId, deviceId, operation (deviceType optional but checked) | device response |
| `CHECK_STATE` | roomId | DVC health |
| `GC` | roomId | self-fix response |
| `REDUCE_THREADS` | roomId | self-fix response |
| `RESET` | roomId | room back to its JSON state |

Lookup order: roomId → deviceId → deviceType → operation → configured behaviour.
Errors (unknown room, device, operation, bad JSON) come back on the response topic too, with the
`requestId` and a `code` such as `ROOM_NOT_FOUND`, `DEVICE_NOT_FOUND`, `DEVICE_TYPE_MISMATCH`,
`UNSUPPORTED_OPERATION`, `INVALID_JSON`.

### Responses

Success (room 101 TV):
```json
{"requestId":"REQ-10001","roomId":"101","deviceId":"1","deviceType":"TV","protocol":"IP","operation":"ON",
 "status":"SUCCESS","message":"TV turned ON successfully","durationMs":0,"timestamp":"2026-09-29T14:30:10.123+05:30"}
```

Failure (room 320 KNX light):
```json
{"requestId":"REQ-KNX-1","roomId":"320","deviceId":"4","deviceType":"LIGHT","protocol":"KNX","operation":"ON",
 "status":"FAILURE",
 "error":{"type":"KnxSecureException","message":"secure session authorization failed",
          "description":"Incorrect UserId, Password or Device Auth Code.",
          "possibleCauses":["Incorrect UserId","Incorrect Password","Incorrect Device Auth Code","Corrupted KNX keyring"],
          "recommendedAction":["Verify configured credentials","Regenerate keyring file if corrupted"]},
 "timestamp":"..."}
```

Health (room 420, `CHECK_STATE`): each issue names the action that fixes it, so an agent can act on it directly.
```json
{"requestId":"HEALTH-420-001","roomId":"420",
 "dvc":{"ip":"10.10.10.12","port":9010,"status":"DEGRADED","threadCount":700,"threadThreshold":500,"threadAlert":true,
        "memory":{"usedMemoryMb":7820,"totalMemoryMb":8500,"usagePercent":92,"thresholdPercent":80,
                  "memoryAlert":true,"memoryLeakDetected":true}},
 "issues":[{"type":"HIGH_THREAD_COUNT","message":"Thread count 700 is above configured threshold 500.",
            "suggestedAction":"REDUCE_THREADS","endpoint":"/api/v1/rooms/420/actions/reduce-threads"},
           {"type":"MEMORY_LEAK","message":"Memory usage is continuously increasing.",
            "suggestedAction":"GC","endpoint":"/api/v1/rooms/420/actions/gc"},
           {"type":"HIGH_MEMORY_USAGE","message":"Memory usage 92% is above threshold 80%.",
            "suggestedAction":"GC","endpoint":"/api/v1/rooms/420/actions/gc"}],
 "timestamp":"..."}
```

## REST APIs

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/commands` | Same body as the MQTT topic |
| GET | `/api/v1/rooms` | All rooms with status, thread count, memory %, failures |
| GET | `/api/v1/rooms/{roomId}` | One room with its devices and failure simulation |
| POST | `/api/v1/rooms/{roomId}/devices/{deviceId}/operations/{op}` | Run an operation; optional JSON body = parameters |
| GET | `/api/v1/rooms/{roomId}/health` | DVC health (same as CHECK_STATE) |
| POST | `/api/v1/rooms/{roomId}/actions/gc` | Self-fix: release leaked memory + `System.gc()` |
| POST | `/api/v1/rooms/{roomId}/actions/reduce-threads` | Self-fix: cancel simulator-owned worker threads |
| POST | `/api/v1/rooms/{roomId}/actions/reset` | Restore the room to its JSON state (between demo runs) |
| GET | `/api/v1/simulation/status` | MQTT connection, JVM thread count |
| GET | `/api/v1/simulation/config` | Loaded JSON (password masked) |
| POST | `/api/v1/simulation/reload` | Re-read the JSON without restarting |
| POST | `/api/v1/simulation/rooms/{roomId}/thread-spike?threads=100` | Break things on purpose |
| POST | `/api/v1/simulation/rooms/{roomId}/memory-leak?mb=500` | Break things on purpose |

Self-fix actions (`/actions/...`) and simulation controls (`/simulation/...`) are deliberately separate.

## Simulated vs real

- **Threads are real.** Leaked threads are parked daemon threads in a pool that belongs to that room
  (`sim-room-420-worker-N`). `reduce-threads` cancels only those tasks; it never touches JVM or Spring threads.
  Room 420 starts with 620 of them, and the JVM thread count visibly drops from ~640 to ~17 after the fix.
  Set `"realThreads": false` under `threadLeak` to use a counter instead.
- **DVC memory is simulated** (8.5 GB would not fit on a laptop). `gc` frees the leaked part back to
  `baselineMemoryMb` and also calls `System.gc()`, reporting the real heap before/after under `jvm`.

## simulator-config.json reference

```jsonc
{
  "mqtt": { "enabled": true, "brokerHost": "10.75.0.118", "brokerPort": 1883, "clientId": "room-simulator",
            "username": null, "password": null,
            "commandTopic": "room/simulator/command", "responseTopic": "room/simulator/response", "qos": 1 },
  "timezone": "Asia/Kolkata",                  // timestamps in responses
  "failureSimulation": { "enabled": false },   // global default; rooms can override
  "rooms": [{
    "roomId": "420",
    "dvc": { "ip": "10.10.10.12", "port": 9010,
             "health": { "threadCount": 700, "baselineThreadCount": 80, "threadThreshold": 500,
                         "memoryUsage": "HIGH",            // LOW 25%, NORMAL 40%, MEDIUM 65%, HIGH 92%, CRITICAL 97%
                         "memoryUsagePercent": 92,         // optional, wins over memoryUsage
                         "memoryThresholdPercent": 80, "totalMemoryMb": 8500, "baselineMemoryMb": 5200,
                         "memoryLeak": true } },
    "failureSimulation": { "enabled": true, "triggerOn": ["SocketTimeoutException"],   // empty = any failure
                           "threadLeak": { "enabled": true, "threadsPerFailure": 50, "maxThreads": 700, "realThreads": true },
                           "memoryLeak": { "enabled": true, "memoryPerFailureMb": 50, "maxMemoryMb": 8000 } },
    "devices": [{
      "deviceId": "1", "deviceType": "TV", "protocol": "IP", "name": "optional",
      "operations": {
        "ON": { "result": "FAILURE", "exception": "SocketTimeoutException", "message": "Connect timed out",
                "description": "...", "possibleCauses": [], "recommendedAction": [],
                "delayMs": 1500,                   // response latency (only while it is failing, for FAILURE ops)
                "failOnlyWhenDegraded": true }     // fails only while the DVC is unhealthy
      }
    }]
  }]
}
```

Everything except `roomId`, `deviceId`, `deviceType` and the operation names has a default.

## Demo rooms in the shipped JSON

- **101**: TV, light, curtain, AC; every operation succeeds (ON/OFF/CHANNEL_UP/DIM/OPEN/CLOSE/SET_TEMPERATURE...).
- **320**: TV `ConnectException`, light `NoRouteToHostException`, curtain `SocketException`, and a KNX secure light (device 4) with `KnxSecureException`, possible causes and recommended actions.
- **420**: DVC starts degraded (700 threads, 92% memory, leak). TV ON times out while degraded.
  Self-heal: `CHECK_STATE` → `GC` → `REDUCE_THREADS` → `CHECK_STATE` shows HEALTHY and TV ON now succeeds. `RESET` puts it back for the next run.
- **520**: starts healthy; every TV ON timeout adds 50 threads and 400 MB, so after 10 attempts it is DEGRADED (550 threads, 87% memory). Shows the degradation building up live.

See `samples/commands.md` for copy-paste `mosquitto_pub` and `curl` commands.

## Project layout

```
src/main/java/com/example/roomsimulator/
  RoomSimulatorApplication.java
  config/      SimulatorProperties, SimulatorConfigLoader (reads + validates the JSON), JacksonConfig
  model/       SimulatorDefinition, MqttSettings, RoomConfig, DvcConfig, HealthConfig, DeviceConfig,
               OperationConfig, FailureSimulationConfig
  dto/         CommandRequest, DeviceResponse, ErrorDetail, HealthResponse, SelfFixResponse, ErrorResponse
  mqtt/        MqttConnectionManager, MqttMessageListener, MqttPublisher
  controller/  RoomController, HealthController, SimulationController
  service/     RoomSimulationService, DeviceSimulationService, HealthSimulationService, SelfFixService,
               CommandDispatcher
  simulator/   DeviceSimulator, FailureSimulator, ThreadLeakSimulator, MemoryLeakSimulator, RoomRuntime
  exception/   SimulatorException, GlobalExceptionHandler
src/main/resources/  application.yml, simulator-config.json
```
