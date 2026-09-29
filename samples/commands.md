# Sample commands

Publish to `room/simulator/command`, read replies on `room/simulator/response`:

```bash
mosquitto_sub -h localhost -t room/simulator/response -v &

mosquitto_pub -h localhost -t room/simulator/command -m '{"command":"EXECUTE","roomId":"101","deviceId":"1","deviceType":"TV","operation":"ON","requestId":"REQ-10001"}'
mosquitto_pub -h localhost -t room/simulator/command -m '{"command":"EXECUTE","roomId":"320","deviceId":"1","deviceType":"TV","operation":"ON","requestId":"REQ-20001"}'
mosquitto_pub -h localhost -t room/simulator/command -m '{"command":"EXECUTE","roomId":"320","deviceId":"4","deviceType":"LIGHT","operation":"ON","requestId":"REQ-KNX-1"}'
mosquitto_pub -h localhost -t room/simulator/command -m '{"command":"EXECUTE","roomId":"101","deviceId":"4","deviceType":"AC","operation":"SET_TEMPERATURE","parameters":{"temperature":22},"requestId":"REQ-AC-1"}'

# Self-healing loop for room 420
mosquitto_pub -h localhost -t room/simulator/command -m '{"command":"CHECK_STATE","roomId":"420","requestId":"HEALTH-420-001"}'
mosquitto_pub -h localhost -t room/simulator/command -m '{"command":"GC","roomId":"420","requestId":"FIX-420-GC"}'
mosquitto_pub -h localhost -t room/simulator/command -m '{"command":"REDUCE_THREADS","roomId":"420","requestId":"FIX-420-THREADS"}'
mosquitto_pub -h localhost -t room/simulator/command -m '{"command":"CHECK_STATE","roomId":"420","requestId":"HEALTH-420-002"}'
mosquitto_pub -h localhost -t room/simulator/command -m '{"command":"RESET","roomId":"420","requestId":"RESET-420"}'
```

Same over REST:

```bash
curl -s -XPOST localhost:8080/api/v1/commands -H 'Content-Type: application/json' \
  -d '{"command":"EXECUTE","roomId":"101","deviceId":"1","deviceType":"TV","operation":"ON","requestId":"REQ-10001"}'

curl -s localhost:8080/api/v1/rooms/420/health
curl -s -XPOST localhost:8080/api/v1/rooms/420/actions/gc
curl -s -XPOST localhost:8080/api/v1/rooms/420/actions/reduce-threads
curl -s localhost:8080/api/v1/rooms/420/health
curl -s -XPOST localhost:8080/api/v1/rooms/420/actions/reset

# Watch room 520 degrade: each TV ON timeout adds 50 threads and 400 MB
for i in $(seq 1 10); do curl -s -XPOST localhost:8080/api/v1/rooms/520/devices/1/operations/ON > /dev/null; done
curl -s localhost:8080/api/v1/rooms/520/health
```
