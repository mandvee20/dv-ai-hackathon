package com.digivalet.agent.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.digivalet.agent.config.MqttConfig;
import com.digivalet.agent.config.SimulatorConfigLoader;
import com.digivalet.agent.dto.FailureMqttEvent;
import com.digivalet.agent.model.FailureEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

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

//   @Autowired
//   private MqttConfig mqttConfig;

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

      // Process only simulator responses
      if (simulatorConfigLoader.getConfig()
              .getMqtt()
              .getResponseTopic()
              .equalsIgnoreCase(topic))
      {
         log.info("Processing MQTT Failure Response: {}, {}",topic,payload);
         processSimulatorResponse(payload);
      }
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
   
   private void processSimulatorResponse(String payload)
   {
      try
      {
         JsonNode response =
                  objectMapper.readTree(payload);

         String status =
                  response.path("status").asText();

         // Only process actual device failures
         if (!"FAILURE".equalsIgnoreCase(status))
         {
            log.debug("Simulator response is not a failure. status={}", status);
            return;
         }

         String requestId =
                  response.path("requestId").asText();

         String roomId =
                  response.path("roomId").asText();

         String deviceId =
                  response.path("deviceId").asText();

         String deviceType =
                  response.path("deviceType").asText();

         String operation =
                  response.path("operation").asText();

         JsonNode error =
                  response.path("error");

         String exception =
                  error.path("type").asText();

         String errorMessage =
                  error.path("message").asText();

         String description =
                  error.path("description").asText();

         log.error(
                  "Room simulator device failure | roomId={} | deviceId={} | deviceType={} | operation={} | exception={} | message={}",
                  roomId,
                  deviceId,
                  deviceType,
                  operation,
                  exception,
                  errorMessage);

         publishFailureEvent(
                  requestId,
                  roomId,
                  deviceId,
                  deviceType,
                  operation,
                  exception,
                  errorMessage,
                  description);
      }
      catch (Exception e)
      {
         log.error("Failed to process simulator response: {}", payload, e);
      }
   }

   public void publishFailureEvent(FailureEvent event)
   {
//      try
//      {
         FailureMqttEvent mqttEvent =
                  new FailureMqttEvent("ipad", "device.disconnect", event.getRoomId(),
                           Instant.now().toString(), event.getRequestId(), "validation",
                           event.getError(), null, Map.of("action", event.getError()));

//         String payload = objectMapper.writeValueAsString(mqttEvent);
//
//         mqttClient.publish(
//                 "room/simulator/failure",
////                 simulatorConfigLoader.getConfig().getMqtt()
////                         .getCommandTopic(),
//                  new MqttMessage(payload.getBytes(StandardCharsets.UTF_8)));
//
//
//         log.info("Failure event published to MQTT requestId={} roomId={}", event.getRequestId(),
//                  event.getRoomId());
//      }
//      catch (Exception e)
//      {
//         log.error("Failed to publish failure event to MQTT requestId={}", event.getRequestId(), e);
//      }
         String topic = "room/simulator/failure";

         try {
            String payload = objectMapper.writeValueAsString(event);

            MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            message.setQos(1);
            message.setRetained(false);

            mqttClient.publish(topic, message);

            log.info(
                    "Failure event published | topic={} | requestId={} | roomId={} | payload={}",
                    topic,
                    event.getRequestId(),
                    event.getRoomId(),
                    payload
            );

         } catch (Exception e) {
            log.error(
                    "Failure event publish failed | topic={} | requestId={} | roomId={}",
                    topic,
                    event.getRequestId(),
                    event.getRoomId(),
                    e
            );
         }
   }
   
   private void publishFailureEvent(
            String requestId,
            String roomId,
            String deviceId,
            String deviceType,
            String operation,
            String exception,
            String message,
            String description)
   {
      try
      {
         Map<String, String> details = new HashMap<>();

         details.put("action", operation);
         details.put("deviceType", deviceType);
         details.put("exception", exception);
         details.put("message", message);
         details.put("description", description);

         FailureMqttEvent event = new FailureMqttEvent();

         event.setSource("validation_service");
         event.setType("device.fail");
         event.setRoomNumber(roomId);
         event.setTs(Instant.now().toString());
         event.setIntentId(requestId);
         event.setIntent("validation");
         event.setErrorCode(exception);
         event.setDeviceId(deviceId);
         event.setDetails(details);

         String payload =
                  objectMapper.writeValueAsString(event);

         /*
          * IMPORTANT:
          * Publish this to the topic where your AI/automation
          * service expects failure events.
          */
         mqttClient.publish(
                  // mqttConfig.getFailureTopic(),
                  "room/simulator/response",
                  new MqttMessage(payload.getBytes(StandardCharsets.UTF_8)));

         log.info(
                  "Failure event published | requestId={} | roomId={} | deviceId={} | deviceType={} | exception={}",
                  requestId,
                  roomId,
                  deviceId,
                  deviceType,
                  exception);
      }
      catch (Exception e)
      {
         log.error(
                  "Failed to publish failure event | requestId={} | roomId={} | deviceId={}",
                  requestId,
                  roomId,
                  deviceId,
                  e);
      }
   }
}
