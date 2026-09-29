package com.example.roomsimulator.mqtt;

import com.example.roomsimulator.dto.CommandRequest;
import com.example.roomsimulator.dto.ErrorResponse;
import com.example.roomsimulator.exception.SimulatorException;
import com.example.roomsimulator.model.MqttSettings;
import com.example.roomsimulator.service.CommandDispatcher;
import com.example.roomsimulator.service.RoomSimulationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handles messages on the common command topic. Work runs off the Paho callback thread so a slow
 * (delayMs) device never blocks the MQTT client.
 */
@Component
public class MqttMessageListener {

    private static final Logger log = LoggerFactory.getLogger(MqttMessageListener.class);

    private final ObjectMapper objectMapper;
    private final CommandDispatcher dispatcher;
    private final MqttPublisher publisher;
    private final RoomSimulationService rooms;
    private final ExecutorService workers = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "mqtt-command-worker");
        t.setDaemon(true);
        return t;
    });

    public MqttMessageListener(ObjectMapper objectMapper, CommandDispatcher dispatcher, MqttPublisher publisher,
                               RoomSimulationService rooms) {
        this.objectMapper = objectMapper;
        this.dispatcher = dispatcher;
        this.publisher = publisher;
        this.rooms = rooms;
    }

    public void onMessage(String topic, MqttMessage message) {
        String body = new String(message.getPayload(), StandardCharsets.UTF_8);
        log.debug("MQTT <- {} {}", topic, body);
        workers.submit(() -> handle(body));
    }

    void handle(String body) {
        MqttSettings mqtt = rooms.definition().mqtt();
        Object response;
        CommandRequest request = null;
        try {
            request = objectMapper.readValue(body, CommandRequest.class);
            response = dispatcher.dispatch(request);
        } catch (SimulatorException e) {
            response = ErrorResponse.of(e.getCode(), e.getMessage())
                    .withRequest(request == null ? null : request.requestId(), request == null ? null : request.roomId());
        } catch (Exception e) {
            response = ErrorResponse.of(request == null ? "INVALID_JSON" : "INTERNAL_ERROR", e.getMessage())
                    .withRequest(request == null ? null : request.requestId(), request == null ? null : request.roomId());
        }
        publisher.publish(mqtt.responseTopic(), response, mqtt.qos());
    }

    @PreDestroy
    public void shutdown() {
        workers.shutdownNow();
    }
}
