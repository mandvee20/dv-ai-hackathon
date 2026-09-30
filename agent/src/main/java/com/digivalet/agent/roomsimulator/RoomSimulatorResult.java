package com.digivalet.agent.roomsimulator;

import java.time.OffsetDateTime;
import lombok.Data;

/**
 * @author Mandvee Vatsa
 * @date Sep 29, 2026
 */
@Data
public class RoomSimulatorResult
{
   private String requestId;
   private String roomId;
   private String deviceId;
   private String deviceType;
   private String operation;
   private String status;
   private String message;
   private OffsetDateTime timestamp;

}
