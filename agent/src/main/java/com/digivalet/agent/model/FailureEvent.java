package com.digivalet.agent.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FailureEvent
{
   private String requestId;

   private String roomId;

   private String intent;

   private String timestamp;

   private String error;

   private List<String> logs;
}
