package com.digivalet.agent.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FailureMqttEvent
{
   private String source;

   @JsonProperty("type")
   private String type;

   @JsonProperty("room_number")
   private String roomNumber;

   private String ts;

   @JsonProperty("intent_id")
   private String intentId;

   private String intent;

   @JsonProperty("error_code")
   private String errorCode;

   @JsonProperty("device-id")
   private String deviceId;

   private Map<String, String> details;
}
