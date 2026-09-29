package com.precheckin.simulator.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FiasRoomStatusScheduler
{
   private static final Logger log = LoggerFactory.getLogger(FiasRoomStatusScheduler.class);

   private final FiasTcpClient fiasTcpClient;

   public FiasRoomStatusScheduler(FiasTcpClient fiasTcpClient)
   {
      this.fiasTcpClient = fiasTcpClient;
   }

   @Scheduled(cron = "0 */2 * * * *", zone = "Asia/Kolkata")
   public void fetchRoomStatus()
   {
      log.info("Starting scheduled FIAS room status fetch");

      List<String> events = fiasTcpClient.fetchRoomStatus();

      log.info("Received {} FIAS room status events", events.size());

      for (String event : events)
      {
         log.info("Processing FIAS event: {}", event);

         // TODO:
         // Parse and store room status
      }
   }
}
