package com.digivalet.agent.roomsimulator;

import lombok.Builder;
import lombok.Data;

/**
 * @author Mandvee Vatsa
 * @date Sep 29, 2026
 */
@Data
@Builder
public class RoomSimulatorCommand
{
   private String command;
   private String roomId;
   private String deviceId;
   private String deviceType;
   private String operation;
   private String requestId;
}
