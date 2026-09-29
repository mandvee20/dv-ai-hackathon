package com.digivalet.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntentRequest
{
   private String requestId;

   private String roomId;

   private IntentType intent;

   private String timestamp;

   private Map<String, Object> parameters;

}
