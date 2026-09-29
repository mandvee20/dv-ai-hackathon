package com.digivalet.agent.config;
/**
 * @author Mandvee Vatsa
 * @date Sep 29, 2026
 */
public class DeviceConfig
{
   private String deviceId;
   private String deviceType;

   private Map<String, OperationConfig> operations;

   public String getDeviceId() {
       return deviceId;
   }

   public void setDeviceId(String deviceId) {
       this.deviceId = deviceId;
   }

   public String getDeviceType() {
       return deviceType;
   }

   public void setDeviceType(String deviceType) {
       this.deviceType = deviceType;
   }

   public Map<String, OperationConfig> getOperations() {
       return operations;
   }

   public void setOperations(Map<String, OperationConfig> operations) {
       this.operations = operations;
   }
}
