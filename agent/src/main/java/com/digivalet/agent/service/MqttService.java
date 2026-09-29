package com.digivalet.agent.service;

import com.digivalet.agent.dto.FailureMqttEvent;
import com.digivalet.agent.model.FailureEvent;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.digivalet.agent.config.MqttConfig;
import com.digivalet.agent.config.SimulatorConfigLoader;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * @author Mandvee Vatsa
 * @date Sep 29, 2026
 */
@Service
@Slf4j
public class MqttService
{

   private MqttClient mqttClient;
   @Autowired
   public SimulatorConfigLoader simulatorConfigLoader;

   @Autowired
   private ObjectMapper objectMapper;

   @Autowired
   private MqttConfig mqttConfig;

   @PostConstruct
   public void connect()
   {

      try
      {

         String brokerUrl =
                  "tcp://" +simulatorConfigLoader.getConfig().getMqtt().getBrokerHost() + ":" + simulatorConfigLoader.getConfig().getMqtt().getBrokerPort();

         mqttClient = new MqttClient(brokerUrl, UUID.randomUUID().toString().substring(0, 8));

         MqttConnectOptions options = new MqttConnectOptions();

         options.setAutomaticReconnect(true);
         options.setCleanSession(true);
         options.setConnectionTimeout(10);
         options.setKeepAliveInterval(60);

         mqttClient.connect(options);

         log.info("Connected to MQTT broker: " + brokerUrl);

         subscribe();

      }
      catch (MqttException e)
      {

         log.error("Failed to connect to MQTT broker");

         e.printStackTrace();
      }
   }

   private void subscribe() throws MqttException
   {

      mqttClient.subscribe(simulatorConfigLoader.getConfig().getMqtt().getResponseTopic(), this::handleMessage);

      log.info("Subscribed to: " + simulatorConfigLoader.getConfig().getMqtt().getResponseTopic());
   }

   private void handleMessage(String topic, MqttMessage message)
   {

      String payload = new String(message.getPayload());

      log.info("MQTT Message received");

      log.info("Topic : " + topic);

      log.info("Payload : " + payload);
   }

   public void publish(String topic, String payload)
   {

      try
      {

         if (!mqttClient.isConnected())
         {
            log.info("MQTT client is not connected");
            return;
         }

         MqttMessage message = new MqttMessage(payload.getBytes());

         message.setQos(1);
         message.setRetained(false);

         mqttClient.publish(topic, message);

         log.info("MQTT message published successfully");
         log.info("Topic   : " + topic);
         log.info("Payload : " + payload);

      }
      catch (MqttException e)
      {

         log.error("Failed to publish MQTT message: " + e.getMessage());

         e.printStackTrace();
      }
   }

   public void publishFailureEvent(FailureEvent event)
   {
      try
      {
         FailureMqttEvent mqttEvent =
                  new FailureMqttEvent("ipad", "device.disconnect", event.getRoomId(),
                           Instant.now().toString(), event.getRequestId(), "validation",
                           event.getError(), null, Map.of("action", event.getError()));

         String payload = objectMapper.writeValueAsString(mqttEvent);

         mqttClient.publish(mqttConfig.getCommandTopic(),
                  new MqttMessage(payload.getBytes(StandardCharsets.UTF_8)));

         log.info("Failure event published to MQTT requestId={} roomId={}", event.getRequestId(),
                  event.getRoomId());
      }
      catch (Exception e)
      {
         log.error("Failed to publish failure event to MQTT requestId={}", event.getRequestId(), e);
      }
   }
}
