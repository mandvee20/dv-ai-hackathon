package com.digivalet.agent.service;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;
import com.digivalet.agent.config.MqttConfig;
import com.digivalet.agent.config.SimulatorConfigLoader;
import jakarta.annotation.PostConstruct;

/**
 * @author Mandvee Vatsa
 * @date Sep 29, 2026
 */
@Service
public class MqttService {

    private MqttClient mqttClient;

    private final MqttConfig mqttConfig;

    public MqttService(SimulatorConfigLoader configLoader) {
        this.mqttConfig = configLoader.getConfig().getMqtt();
    }

    @PostConstruct
    public void connect() {

        try {

            String brokerUrl =
                    "tcp://" +
                    mqttConfig.getBrokerHost() +
                    ":" +
                    mqttConfig.getBrokerPort();

            mqttClient = new MqttClient(
                    brokerUrl,
                    mqttConfig.getClientId()
            );

            MqttConnectOptions options =
                    new MqttConnectOptions();

            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(60);

            mqttClient.connect(options);

            System.out.println(
                    "Connected to MQTT broker: " + brokerUrl
            );

            subscribe();

        } catch (MqttException e) {

            System.err.println(
                    "Failed to connect to MQTT broker"
            );

            e.printStackTrace();
        }
    }

    private void subscribe() throws MqttException {

        mqttClient.subscribe(
                mqttConfig.getCommandTopic(),
                this::handleMessage
        );

        System.out.println(
                "Subscribed to: " +
                mqttConfig.getCommandTopic()
        );
    }

    private void handleMessage(
            String topic,
            MqttMessage message) {

        String payload =
                new String(message.getPayload());

        System.out.println(
                "MQTT Message received"
        );

        System.out.println(
                "Topic : " + topic
        );

        System.out.println(
                "Payload : " + payload
        );
    }
}
