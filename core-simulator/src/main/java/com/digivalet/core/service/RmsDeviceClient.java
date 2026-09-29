package com.digivalet.core.service;

import com.digivalet.core.model.IntentRequest;
import com.digivalet.core.model.RmsCommand;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RmsDeviceClient
{
   private final MqttClient mqttClient;

   private final String commandTopic;

   public RmsDeviceClient(@Value("${mqtt.broker:tcp://localhost:1883}") String broker,
            @Value("${mqtt.client-id:core-simulator}") String clientId,
            @Value("${mqtt.rms-command-topic:room/simulator/command}") String commandTopic)
            throws MqttException
   {
      this.commandTopic = commandTopic;

      mqttClient = new MqttClient(broker, clientId);

      MqttConnectOptions options = new MqttConnectOptions();
      options.setAutomaticReconnect(true);
      options.setCleanSession(true);

      mqttClient.connect(options);

      log.info("Connected to MQTT broker for RMS device communication");
   }

   public void execute(IntentRequest request)
   {
      try
      {
         String deviceId = String.valueOf(request.getParameters().get("deviceId"));

         RmsCommand command = new RmsCommand(request.getRequestId(), request.getRoomId(), deviceId,
                  request.getIntent().name());

         String payload =
                  new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(command);

         mqttClient.publish(commandTopic, payload.getBytes(), 1, false);

         log.info("RMS command sent. requestId={}, roomId={}, deviceId={}, command={}",
                  request.getRequestId(), request.getRoomId(), deviceId, request.getIntent());
      }
      catch (Exception e)
      {
         log.error("Failed to send RMS command. requestId={}, roomId={}", request.getRequestId(),
                  request.getRoomId(), e);

         throw new RuntimeException("Failed to send command to RMS device", e);
      }
   }
}
