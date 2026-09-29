package com.example.roomsimulator.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Publishes JSON responses. The client is handed over by {@link MqttConnectionManager}. */
@Component
public class MqttPublisher {

    private static final Logger log = LoggerFactory.getLogger(MqttPublisher.class);

    private final ObjectMapper objectMapper;
    private volatile MqttClient client;

    public MqttPublisher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    void attach(MqttClient client) {
        this.client = client;
    }

    public boolean publish(String topic, Object payload, int qos) {
        MqttClient current = client;
        if (current == null || !current.isConnected()) {
            log.warn("MQTT not connected; dropping message for {}", topic);
            return false;
        }
        try {
            byte[] body = objectMapper.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8);
            MqttMessage message = new MqttMessage(body);
            message.setQos(qos);
            current.publish(topic, message);
            log.debug("MQTT -> {} {}", topic, new String(body, StandardCharsets.UTF_8));
            return true;
        } catch (JsonProcessingException | MqttException e) {
            log.error("Failed to publish to {}: {}", topic, e.getMessage());
            return false;
        }
    }
}
