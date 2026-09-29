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
      List<String> events = new ArrayList<>();

      // Room 101 is always Clean/Vacant
      events.add("RE|RN101|RS3|");
      for (int i = 0; i < 4; i++)
      {
         String roomNumber = String.valueOf(100 + random.nextInt(900));

         int roomStatus;

         do
         {
            roomStatus = 1 + random.nextInt(6);
         } while (roomStatus == 3);

         String event = "RE|RN" + roomNumber + "|RS" + roomStatus + "|";

         events.add(event);
      }
      return events;
   }
}
