package com.digivalet.agent.config;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Mandvee Vatsa
 * @date Sep 29, 2026
 */
public class RoomConfig
{
   private String roomId;
   private DvcConfig dvc;
   private List<DeviceConfig> devices;

   public String getRoomId() {
       return roomId;
   }

   public void setRoomId(String roomId) {
       this.roomId = roomId;
   }

   public DvcConfig getDvc() {
       return dvc;
   }

   public void setDvc(DvcConfig dvc) {
       this.dvc = dvc;
   }

   public List<DeviceConfig> getDevices() {
       return devices;
   }

   public void setDevices(List<DeviceConfig> devices) {
       this.devices = devices;
   }
   public Map<String, DeviceConfig> getDeviceMap() {

      return devices.stream()
              .collect(Collectors.toMap(
                      DeviceConfig::getDeviceId,
                      Function.identity()
              ));
  }
}
