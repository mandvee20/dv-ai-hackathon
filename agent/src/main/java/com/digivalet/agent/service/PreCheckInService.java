package com.digivalet.agent.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.digivalet.agent.config.DeviceConfig;
import com.digivalet.agent.config.RoomConfig;
import com.digivalet.agent.config.RoomRegistry;
import com.digivalet.agent.config.SimulatorConfigLoader;
import com.digivalet.agent.dto.PreCheckInRequest;
import com.digivalet.agent.dto.RoomSimulatorCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Mandvee Vatsa
 * @date Sep 29, 2026
 */
@Service
@Slf4j
public class PreCheckInService
{
   @Autowired
   public MqttService mqttService;
   @Autowired
   public SimulatorConfigLoader simulatorConfigLoader;
   @Autowired
   private ObjectMapper objectMapper;

   @Autowired
   private RoomRegistry roomRegistry;
   private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(5);

   public void processPreCheckIn(PreCheckInRequest request)
   {

      log.info("Received pre-check-in request for reservationNumber={}",
               request.getReservationNumber());

      LocalDateTime arrivalDateTime = LocalDateTime.of(request.getArrivalDate(),
               LocalTime.parse(request.getEstimatedArrivalTime()));

      LocalDateTime executionTime = arrivalDateTime.minusMinutes(30);

      long delay =
               Duration.between(LocalDateTime.now(), executionTime).toMillis();

      if (delay <= 0)
      {
         log.info("30-minute pre-check-in time has already passed. "
                  + "Executing room validation immediately.");

         executeRoomValidation(request);
         return;
      }

      log.info("Room validation scheduled for {}", executionTime);

      scheduler.schedule(() -> executeRoomValidation(request), delay,
               TimeUnit.MILLISECONDS);
   }

   // private void executeRoomValidation(PreCheckInRequest request)
   // {
   //
   // try
   // {
   //
   // String requestId = "REQ-" + UUID.randomUUID();
   //
   // RoomSimulatorCommand command =
   // RoomSimulatorCommand.builder().command("EXECUTE")
   // .roomId(request.getRoomNumber()).deviceId("1").deviceType("TV")
   // .operation("ON").requestId(requestId).build();
   //
   // String payload = objectMapper.writeValueAsString(command);
   //
   // mqttService.publish("room/simulator/command", payload);
   //
   // log.info("Room simulator command sent | requestId={} | roomId={}",
   // requestId, command.getRoomId());
   //
   // }
   // catch (Exception e)
   // {
   //
   // log.error("Failed to send room simulator command", e);
   // }
   // }

   private void executeRoomValidation(PreCheckInRequest request)
   {

      try
      {

         String roomId = request.getRoomNumber();

         RoomConfig room = roomRegistry.getRoom(roomId);
         log.info("ROOM CONFIG: {}", room);
         if (room == null)
         {
            log.warn("No room configuration found | roomId={}", roomId);
            return;
         }

         log.info("Starting room validation | roomId={} | deviceCount={}",
                  roomId, room.getDevices().size());

         // -------------------------------------------------
         // 1. Execute commands for all devices
         // -------------------------------------------------

         log.info("ROOM DEVICES: {}", room.getDevices());
         for (DeviceConfig device : room.getDevices())
         {

            // Pick the configured operation
            String operation = device.getOperations().keySet().stream()
                     .findFirst().orElse(null);

            if (operation == null)
            {

               log.warn("No operation configured | roomId={} | deviceId={}",
                        roomId, device.getDeviceId());

               continue;
            }

            String requestId = "REQ-" + UUID.randomUUID();

            RoomSimulatorCommand command = RoomSimulatorCommand.builder()
                     .command("EXECUTE").roomId(roomId)
                     .deviceId(device.getDeviceId())
                     .deviceType(device.getDeviceType()).operation(operation)
                     .requestId(requestId).build();

            String payload = objectMapper.writeValueAsString(command);

            mqttService.publish(simulatorConfigLoader.getConfig().getMqtt()
                     .getCommandTopic(), payload);

            log.info("Room simulator command sent | requestId={} | roomId={} | deviceId={} | deviceType={} | operation={}",
                     requestId, roomId, device.getDeviceId(),
                     device.getDeviceType(), operation);
         }

         // -------------------------------------------------
         // 2. Check DVC health
         // -------------------------------------------------

         String healthRequestId = "HEALTH-" + roomId + "-" + UUID.randomUUID();

         RoomSimulatorCommand healthCommand =
                  RoomSimulatorCommand.builder().command("CHECK_STATE")
                           .roomId(roomId).requestId(healthRequestId).build();

         String healthPayload = objectMapper.writeValueAsString(healthCommand);

         mqttService.publish(
                  simulatorConfigLoader.getConfig().getMqtt().getCommandTopic(),
                  healthPayload);

         log.info("DVC health check command sent | requestId={} | roomId={}",
                  healthRequestId, roomId);

      }
      catch (Exception e)
      {

         log.error("Failed to execute room validation | roomId={}",
                  request.getRoomNumber(), e);
      }
   }
}
