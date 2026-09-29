package com.digivalet.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomSimulatorCommand
{
   private String command;
   private String roomId;
   private String deviceId;
   private String deviceType;
   private String operation;
   private String requestId;
}
