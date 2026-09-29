package com.digivalet.core.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;

import com.digivalet.core.model.FailureEvent;
import com.digivalet.core.model.IntentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class FailureEventPublisher
{
   private static final String TOPIC = "intent/failure";

   private final MqttClient mqttClient;

   private final ObjectMapper objectMapper;

   public FailureEventPublisher(MqttClient mqttClient, ObjectMapper objectMapper)
   {
      this.mqttClient = mqttClient;
      this.objectMapper = objectMapper;
   }

   public void publish(IntentRequest request, String error)
   {
      try
      {
         FailureEvent event = new FailureEvent(
                  request.getRequestId(),
                  request.getRoomId(),
                  request.getIntent().name(),
                  Instant.now().toString(),
                  error);

         String payload = objectMapper.writeValueAsString(event);

         MqttMessage message = new MqttMessage(
                  payload.getBytes(StandardCharsets.UTF_8));

         message.setQos(1);

         mqttClient.publish(TOPIC, message);

         log.info(
                  "Failure event published requestId={} roomId={} intent={}",
                  request.getRequestId(),
                  request.getRoomId(),
                  request.getIntent());
      }
      catch (Exception e)
      {
         throw new IllegalStateException(
                  "Unable to publish failure event", e);
      }
   }
}
