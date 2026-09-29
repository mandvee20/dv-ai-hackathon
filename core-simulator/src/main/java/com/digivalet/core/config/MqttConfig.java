package com.digivalet.core.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttConfig
{
   @Bean
   public MqttClient mqttClient() throws Exception
   {
      MqttClient client = new MqttClient("tcp://localhost:1883", "core-simulator");

      client.connect();

      return client;
   }
}
