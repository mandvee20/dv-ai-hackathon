package com.digivalet.core.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FailureEvent
{
   private String requestId;

   private String roomId;

   private String intent;

   private String timestamp;

   private String error;

   private List<String> logs;

}
