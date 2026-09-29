package com.digivalet.agent.scheduler;
import com.digivalet.agent.config.DeviceConfig;
import com.digivalet.agent.config.RoomConfig;
import com.digivalet.agent.config.SimulatorConfig;
import com.digivalet.agent.config.SimulatorConfigLoader;
import com.digivalet.agent.dto.RoomSimulatorCommand;
import com.digivalet.agent.service.MqttService;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Component
public class FiasRoomStatusScheduler
{
   private static final Logger log = LoggerFactory.getLogger(FiasRoomStatusScheduler.class);

   private final FiasTcpClient fiasTcpClient;

   private final SimulatorConfigLoader simulatorConfigLoader;

   private final MqttService mqttService;

   private final Random random = new Random();

   public FiasRoomStatusScheduler(
            FiasTcpClient fiasTcpClient,
            SimulatorConfigLoader simulatorConfigLoader , MqttService mqttService)
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

      // Send TV command
      devices.stream().filter(device -> "TV".equalsIgnoreCase(device.getDeviceType())).findFirst()
               .ifPresent(device -> {
                  sendDeviceCommand(roomNumber, device, "ON");
               });

      // Find all configured lights
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

      sendDeviceCommand(roomNumber, randomLight, "ON");
   }

   private void sendDeviceCommand(String roomNumber, DeviceConfig device, String operation)
   {
      RoomSimulatorCommand command = new RoomSimulatorCommand();

      command.setCommand("EXECUTE");
      command.setRoomId(roomNumber);
      command.setDeviceId(device.getDeviceId());
      command.setDeviceType(device.getDeviceType());
      command.setOperation(operation);
      command.setRequestId("REQ-" + System.currentTimeMillis());

      log.info("Sending device command. Room: {}, Device: {}, Type: {}, Operation: {}", roomNumber,
               device.getDeviceId(), device.getDeviceType(), operation);

      mqttService.publish("room/simulator/command", command.toString());
   }

}
