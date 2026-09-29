package com.example.roomsimulator.mqtt;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.example.roomsimulator.model.MqttSettings;
import com.example.roomsimulator.service.RoomSimulationService;
import jakarta.annotation.PreDestroy;

/**
 * Connects to the broker from simulator-config.json and subscribes to the common command topic.
 * If the broker is down at startup the REST APIs still work and the connection keeps retrying.
 */
@Component
public class MqttConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(MqttConnectionManager.class);
    private static final long RETRY_SECONDS = 5;

    private final RoomSimulationService rooms;
    private final MqttMessageListener listener;
    private final MqttPublisher publisher;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "mqtt-connector");
        t.setDaemon(true);
        return t;
    });
    private volatile MqttClient client;
    private volatile String lastError;

    public MqttConnectionManager(RoomSimulationService rooms, MqttMessageListener listener, MqttPublisher publisher) {
        this.rooms = rooms;
        this.listener = listener;
        this.publisher = publisher;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        MqttSettings mqtt = rooms.definition().mqtt();
        if (!mqtt.enabled()) {
            log.info("MQTT disabled in config; REST APIs only");
            return;
        }
        scheduler.execute(this::connect);
    }

    private void connect() {
        MqttSettings mqtt = rooms.definition().mqtt();
        try {
           String clientId = mqtt.clientId() + "-" + UUID.randomUUID().toString().substring(0, 8);
           MqttClient c = new MqttClient(mqtt.brokerUrl(), clientId, new MemoryPersistence());            
           MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(5);
            options.setKeepAliveInterval(30);
            if (mqtt.username() != null && !mqtt.username().isBlank()) {
                options.setUserName(mqtt.username());
                options.setPassword(mqtt.password() == null ? new char[0] : mqtt.password().toCharArray());
            }
            c.setCallback(new Callback(mqtt));
            c.connect(options);
            client = c;
            publisher.attach(c);
            subscribe(mqtt);
            lastError = null;
        } catch (MqttException e) {
            lastError = e.getCause() != null ? e.getMessage() + ": " + e.getCause().getMessage() : e.getMessage();
            log.warn("MQTT connect to {} failed ({}); retrying in {}s", mqtt.brokerUrl(), lastError, RETRY_SECONDS);
            scheduler.schedule(this::connect, RETRY_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void subscribe(MqttSettings mqtt) throws MqttException {
        client.subscribe(mqtt.commandTopic(), mqtt.qos(), listener::onMessage);
        log.info("MQTT connected to {}; listening on {} and replying on {}", mqtt.brokerUrl(), mqtt.commandTopic(),
                mqtt.responseTopic());
    }

    public boolean isConnected() {
        MqttClient c = client;
        return c != null && c.isConnected();
    }

    public Map<String, Object> status() {
        MqttSettings mqtt = rooms.definition().mqtt();
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("enabled", mqtt.enabled());
        status.put("broker", mqtt.brokerUrl());
        status.put("connected", isConnected());
        status.put("commandTopic", mqtt.commandTopic());
        status.put("responseTopic", mqtt.responseTopic());
        if (lastError != null) {
            status.put("lastError", lastError);
        }
        return status;
    }

    @PreDestroy
    public void stop() {
        scheduler.shutdownNow();
        MqttClient c = client;
        if (c != null) {
            try {
                if (c.isConnected()) {
                    c.disconnect(10000);
                }
                c.close();
            } catch (MqttException e) {
                log.debug("MQTT close: {}", e.getMessage());
            }
        }
    }

    private final class Callback implements MqttCallbackExtended {
        private final MqttSettings mqtt;

        Callback(MqttSettings mqtt) {
            this.mqtt = mqtt;
        }

        
        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            if (reconnect) {
                try {
                    subscribe(mqtt); // clean session: subscriptions are gone after a reconnect
                } catch (MqttException e) {
                    log.error("Re-subscribe failed: {}", e.getMessage());
                }
            }
        }

        @Override
        public void connectionLost(Throwable cause) {
           lastError = cause == null ? "connection lost"
                    : cause.getCause() != null ? cause.getMessage() + ": " + cause.getCause() : cause.getMessage();
            log.warn("MQTT connection lost: {}", lastError);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            listener.onMessage(topic, message);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
        }
    }
}
