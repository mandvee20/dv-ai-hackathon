package com.digivalet.agent.config;

import java.util.List;

/**
 * @author Mandvee Vatsa
 * @date Sep 29, 2026
 */
public class SimulatorConfig
{
   private MqttConfig mqtt;
   private List<RoomConfig> rooms;

   public MqttConfig getMqtt() {
       return mqtt;
   }

   public void setMqtt(MqttConfig mqtt) {
       this.mqtt = mqtt;
   }

   public List<RoomConfig> getRooms() {
       return rooms;
   }

   public void setRooms(List<RoomConfig> rooms) {
       this.rooms = rooms;
   }
}
