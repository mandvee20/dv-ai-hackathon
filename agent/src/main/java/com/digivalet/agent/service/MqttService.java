package com.digivalet.agent.service;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;
import com.digivalet.agent.config.MqttConfig;
import com.digivalet.agent.config.SimulatorConfigLoader;
import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
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

   private final MqttConfig mqttConfig;

   public MqttService(SimulatorConfigLoader configLoader)
   {
      this.mqttConfig = configLoader.getConfig().getMqtt();
   }

   @PostConstruct
   public void connect()
   {

      try
      {

         String brokerUrl =
                  "tcp://" + mqttConfig.getBrokerHost() + ":" + mqttConfig.getBrokerPort();

         mqttClient = new MqttClient(brokerUrl, mqttConfig.getClientId());

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

      mqttClient.subscribe(mqttConfig.getResponseTopic(), this::handleMessage);

      log.info("Subscribed to: " + mqttConfig.getResponseTopic());
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
}
