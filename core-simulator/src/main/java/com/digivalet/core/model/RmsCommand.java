package com.digivalet.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RmsCommand
{
   private String requestId;

   private String roomId;

   private String deviceId;

   private String command;
}
