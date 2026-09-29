package com.digivalet.agent.scheduler;

import com.digivalet.agent.config.*;
import com.digivalet.agent.dto.FailureMqttEvent;
import com.digivalet.agent.dto.RoomSimulatorCommand;
import com.digivalet.agent.service.MqttService;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.*;

@Component
public class FiasRoomStatusScheduler
{
   private static final Logger log = LoggerFactory.getLogger(FiasRoomStatusScheduler.class);

   private final FiasTcpClient fiasTcpClient;

   private final SimulatorConfigLoader simulatorConfigLoader;

   private final MqttService mqttService;

   private final Random random = new Random();

   @Autowired
   private ObjectMapper objectMapper;

   public FiasRoomStatusScheduler(FiasTcpClient fiasTcpClient,
            SimulatorConfigLoader simulatorConfigLoader, MqttService mqttService)
   {
      this.fiasTcpClient = fiasTcpClient;
      this.simulatorConfigLoader = simulatorConfigLoader;
      this.mqttService = mqttService;
   }

   @Scheduled(cron = "0 */2 * * * *", zone = "Asia/Kolkata")
   public void fetchRoomStatus()
   {
      log.info("Starting scheduled FIAS room status fetch");

      List<String> events = fiasTcpClient.fetchRoomStatus();

      log.info("Received {} FIAS room status events", events.size());

      for (String event : events)
      {
         log.info("Processing FIAS event: {}", event);
         processRoomStatusEvent(event);
      }
   }

   private void processRoomStatusEvent(String event)
   {
      // Example:
      // RE|RN101|RS3|

      String[] parts = event.split("\\|");

      if (parts.length < 3)
      {
         log.warn("Invalid FIAS room status event: {}", event);

         return;
      }

      String roomNumber = parts[1].substring(2);

      int roomStatus = Integer.parseInt(parts[2].substring(2));

      // Process only Clean/Vacant
      if (roomStatus != 3)
      {
         log.debug("Ignoring room {} with status {}", roomNumber, roomStatus);

         return;
      }

      log.info("Room {} is ready for pre-check-in processing", roomNumber);

      processReadyRoom(roomNumber);
   }

   private void processReadyRoom(String roomNumber)
   {
      log.info("Starting device soft-check for room {}", roomNumber);

      SimulatorConfig config = simulatorConfigLoader.getConfig();

      Optional<RoomConfig> roomConfigOptional =
               config.getRooms().stream().filter(room -> roomNumber.equals(room.getRoomId()))
                        .findFirst();

      if (roomConfigOptional.isEmpty())
      {
         log.warn("No configuration found for room {}", roomNumber);

         return;
      }

      RoomConfig roomConfig = roomConfigOptional.get();

      List<DeviceConfig> devices = roomConfig.getDevices();

      if (devices == null || devices.isEmpty())
      {
         log.warn("No devices configured for room {}", roomNumber);

         return;
      }

      // Check TV
      devices.stream().filter(device -> "TV".equalsIgnoreCase(device.getDeviceType())).findFirst()
               .ifPresent(device -> processConfiguredOperation(roomNumber, device, "ON"));

      // Check all configured lights
      List<DeviceConfig> lights =
               devices.stream().filter(device -> "LIGHT".equalsIgnoreCase(device.getDeviceType()))
                        .toList();

      if (lights.isEmpty())
      {
         log.warn("No lights configured for room {}", roomNumber);

         return;
      }

      // Pick a random light
      DeviceConfig randomLight = lights.get(random.nextInt(lights.size()));

      processConfiguredOperation(roomNumber, randomLight, "ON");
   }

   private void processConfiguredOperation(String roomNumber, DeviceConfig device, String operation)
   {
      if (device.getOperations() == null || !device.getOperations().containsKey(operation))
      {
         log.warn("No operation configuration found. Room: {}, Device: {}, Operation: {}",
                  roomNumber, device.getDeviceId(), operation);

         return;
      }

      OperationConfig operationConfig = device.getOperations().get(operation);

      if (!"FAILURE".equalsIgnoreCase(operationConfig.getResult()))
      {
         log.info("Device soft-check successful. Room: {}, Device: {}, Type: {}, Operation: {}",
                  roomNumber, device.getDeviceId(), device.getDeviceType(), operation);

         return;
      }

      log.error(
               "Device soft-check failed. Room: {}, Device: {}, Type: {}, Operation: {}, Exception: {}, Message: {}",
               roomNumber, device.getDeviceId(), device.getDeviceType(), operation,
               operationConfig.getException(), operationConfig.getMessage());

      publishFailureEvent(roomNumber, device, operation, operationConfig);
   }

   private void publishFailureEvent(String roomNumber, DeviceConfig device, String operation,
            OperationConfig operationConfig)
   {
      Map<String, String> details = new HashMap<>();

      details.put("action", operation);
      details.put("exception", operationConfig.getException());
      details.put("message", operationConfig.getMessage());
      details.put("description", operationConfig.getDescription());

      FailureMqttEvent event = new FailureMqttEvent();

      event.setSource("validation_service");
      event.setType("auth.fail");
      event.setRoomNumber(roomNumber);
      event.setTs(Instant.now().toString());
      event.setIntentId("REQ-" + System.currentTimeMillis());
      event.setIntent("validation");
      event.setErrorCode(operationConfig.getException());
      event.setDeviceId(device.getDeviceId());
      event.setDetails(details);

      try
      {
         String payload = objectMapper.writeValueAsString(event);

         mqttService.publish("room/simulator/response", payload);

         log.info("Published device failure event to MQTT. Room: {}, Device: {}, Operation: {}",
                  roomNumber, device.getDeviceId(), operation);
      }
      catch (Exception e)
      {
         log.error("Failed to publish device failure event to MQTT. Room: {}, Device: {}",
                  roomNumber, device.getDeviceId(), e);
      }
   }

}
