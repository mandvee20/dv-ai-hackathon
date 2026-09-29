package com.example.roomsimulator;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.roomsimulator.mqtt.MqttConnectionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.moquette.broker.Server;
import io.moquette.broker.config.MemoryConfig;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** Publishes commands on the common topic of an embedded broker and reads the simulator's responses. */
@SpringBootTest
class MqttIntegrationTest {

    private static Server broker;
    private static int port;

    @Autowired
    private MqttConnectionManager connection;

    private final ObjectMapper mapper = new ObjectMapper();

    @DynamicPropertySource
    static void broker(DynamicPropertyRegistry registry) throws IOException {
        try (ServerSocket s = new ServerSocket(0)) {
            port = s.getLocalPort();
        }
        Properties props = new Properties();
        props.setProperty("port", String.valueOf(port));
        props.setProperty("host", "127.0.0.1");
        props.setProperty("allow_anonymous", "true");
        props.setProperty("persistence_enabled", "false");
        props.setProperty("data_path", Files.createTempDirectory("moquette").toString());
        props.setProperty("websocket_port", "disabled");
        broker = new Server();
        broker.startServer(new MemoryConfig(props));
        registry.add("simulator.mqtt.enabled", () -> "true");
        registry.add("simulator.mqtt.broker-host", () -> "127.0.0.1");
        registry.add("simulator.mqtt.broker-port", () -> String.valueOf(port));
    }

    @AfterAll
    static void stopBroker() {
        if (broker != null) {
            broker.stopServer();
        }
    }

    @Test
    void commandsOnTheCommonTopicGetResponses() throws Exception {
        long deadline = System.currentTimeMillis() + 15_000;
        while (!connection.isConnected() && System.currentTimeMillis() < deadline) {
            Thread.sleep(100);
        }
        assertThat(connection.isConnected()).isTrue();

        BlockingQueue<JsonNode> responses = new LinkedBlockingQueue<>();
        MqttClient client = new MqttClient("tcp://127.0.0.1:" + port, "it-" + System.nanoTime(), new MemoryPersistence());
        client.connect();
        client.subscribe("room/simulator/response", 1,
                (topic, msg) -> responses.add(mapper.readTree(new String(msg.getPayload(), StandardCharsets.UTF_8))));

        send(client, """
                {"command":"EXECUTE","roomId":"101","deviceId":"1","deviceType":"TV","operation":"ON","requestId":"REQ-10001"}""");
        JsonNode ok = responses.poll(10, TimeUnit.SECONDS);
        assertThat(ok).isNotNull();
        assertThat(ok.path("requestId").asText()).isEqualTo("REQ-10001");
        assertThat(ok.path("status").asText()).isEqualTo("SUCCESS");

        send(client, """
                {"command":"CHECK_STATE","roomId":"420","requestId":"HEALTH-420-001"}""");
        JsonNode health = responses.poll(10, TimeUnit.SECONDS);
        assertThat(health).isNotNull();
        assertThat(health.path("dvc").path("status").asText()).isEqualTo("DEGRADED");

        send(client, "{\"command\":\"CHECK_STATE\",\"roomId\":\"999\",\"requestId\":\"BAD-1\"}");
        JsonNode error = responses.poll(10, TimeUnit.SECONDS);
        assertThat(error).isNotNull();
        assertThat(error.path("requestId").asText()).isEqualTo("BAD-1");
        assertThat(error.path("code").asText()).isEqualTo("ROOM_NOT_FOUND");

        send(client, "not json");
        JsonNode invalid = responses.poll(10, TimeUnit.SECONDS);
        assertThat(invalid).isNotNull();
        assertThat(invalid.path("code").asText()).isEqualTo("INVALID_JSON");

        client.disconnect();
        client.close();
    }

    private static void send(MqttClient client, String json) throws Exception {
        MqttMessage message = new MqttMessage(json.getBytes(StandardCharsets.UTF_8));
        message.setQos(1);
        client.publish("room/simulator/command", message);
    }
}
