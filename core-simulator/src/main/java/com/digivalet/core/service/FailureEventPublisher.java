package com.digivalet.core.service;

import java.time.Instant;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;

import com.digivalet.core.model.FailureEvent;
import com.digivalet.core.model.IntentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
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
         FailureEvent event = new FailureEvent(request.getRequestId(), request.getRoomId(),
                  request.getIntent().name(), Instant.now().toString(), error);

         String payload = objectMapper.writeValueAsString(event);

         MqttMessage message = new MqttMessage(payload.getBytes());

         message.setQos(1);

         mqttClient.publish(TOPIC, message);
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Unable to publish failure event", e);
      }
   }
}
