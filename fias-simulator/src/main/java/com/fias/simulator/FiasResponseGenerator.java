package com.fias.simulator;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class FiasResponseGenerator
{
   private final Random random = new Random();

   public List<String> generateRoomStatusEvents()
   {
      int roomCount = 5 + random.nextInt(6);

      List<String> events = new ArrayList<>();

      for (int i = 0; i < roomCount; i++)
      {
         String roomNumber = "101";

         int roomStatus = 3;

         String event = "RE|RN" + roomNumber + "|RS" + roomStatus + "|";

         events.add(event);
      }

      return events;
   }
}
