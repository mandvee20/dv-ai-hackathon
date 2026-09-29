package com.digivalet.core.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.digivalet.core.model.IntentRequest;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RmsDeviceClient
{
   private final Map<String, String> deviceState = new ConcurrentHashMap<>();

   public void execute(IntentRequest request)
   {
      String deviceId = String.valueOf(
               request.getParameters().get("deviceId"));

      String command = request.getIntent().name();

      log.info(
               "Processing RMS command requestId={} roomId={} deviceId={} command={}",
               request.getRequestId(),
               request.getRoomId(),
               deviceId,
               command);

      String newState = getState(command);

      deviceState.put(deviceId, newState);

      log.info(
               "RMS device state updated requestId={} roomId={} deviceId={} state={}",
               request.getRequestId(),
               request.getRoomId(),
               deviceId,
               newState);
   }

   private String getState(String command)
   {
      return switch (command)
      {
         case "LIGHT_ON" -> "ON";
         case "LIGHT_OFF" -> "OFF";
         case "CURTAIN_OPEN" -> "OPEN";
         case "CURTAIN_CLOSE" -> "CLOSED";

         default -> throw new IllegalArgumentException(
                  "Unsupported RMS command: " + command);
      };
   }

   public String getDeviceState(String deviceId)
   {
      return deviceState.get(deviceId);
   }
}
