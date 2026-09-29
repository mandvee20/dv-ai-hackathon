package com.example.roomsimulator.model;

public record MqttSettings(
        Boolean enabled,
        String brokerHost,
        Integer brokerPort,
        String clientId,
        String username,
        String password,
        String commandTopic,
        String responseTopic,
        Integer qos) {

    public MqttSettings {
        enabled = enabled == null ? Boolean.TRUE : enabled;
        brokerHost = brokerHost == null ? "localhost" : brokerHost;
        brokerPort = brokerPort == null ? 1883 : brokerPort;
        clientId = clientId == null ? "room-simulator" : clientId;
        commandTopic = commandTopic == null ? "room/simulator/command" : commandTopic;
        responseTopic = responseTopic == null ? "room/simulator/response" : responseTopic;
        qos = qos == null ? 1 : qos;
    }

    public String brokerUrl() {
        return "tcp://" + brokerHost + ":" + brokerPort;
    }

    public MqttSettings withBroker(String host, Integer port) {
        return new MqttSettings(enabled, host == null || host.isBlank() ? brokerHost : host,
                port == null ? brokerPort : port, clientId, username, password, commandTopic, responseTopic, qos);
    }

    public MqttSettings withEnabled(boolean value) {
        return new MqttSettings(value, brokerHost, brokerPort, clientId, username, password, commandTopic, responseTopic, qos);
    }
}
