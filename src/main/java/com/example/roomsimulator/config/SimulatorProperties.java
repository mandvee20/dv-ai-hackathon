package com.example.roomsimulator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Application-level settings from application.yml. Room and MQTT details stay in the JSON;
 * the optional mqtt overrides here only exist so Docker / a laptop can point at another broker
 * without editing the JSON (e.g. SIMULATOR_MQTT_BROKER_HOST=localhost).
 */
@ConfigurationProperties(prefix = "simulator")
public record SimulatorProperties(String configFile, MqttOverride mqtt) {

    public SimulatorProperties {
        configFile = configFile == null ? "classpath:simulator-config.json" : configFile;
        mqtt = mqtt == null ? new MqttOverride(null, null, null) : mqtt;
    }

    public record MqttOverride(Boolean enabled, String brokerHost, Integer brokerPort) {
    }
}
