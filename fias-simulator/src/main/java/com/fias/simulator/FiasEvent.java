package com.fias.simulator;

public record FiasEvent(String roomNumber, int roomStatus)
{

   public String toMessage()
   {
      return "RE|RN" + roomNumber + "|RS" + roomStatus + "|";
   }

   public String statusDescription()
   {

      return switch (roomStatus)
      {

         case 1 -> "Dirty/Vacant";

         case 2 -> "Dirty/Occupied";

         case 3 -> "Clean/Vacant";

         case 4 -> "Clean/Occupied";

         case 5 -> "Inspected/Vacant";

         case 6 -> "Inspected/Occupied";

         default -> "Unknown";
      };
   }
}
