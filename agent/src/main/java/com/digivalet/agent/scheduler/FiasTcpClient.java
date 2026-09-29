package com.precheckin.simulator.scheduler;

import com.precheckin.simulator.dto.FiasRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@Component
public class FiasTcpClient
{
   private static final Logger log = LoggerFactory.getLogger(FiasTcpClient.class);

   private static final String HOST = "localhost";

   private static final int PORT = 9000;

   private static final int CONNECTION_TIMEOUT_MS = 5000;

   private final ObjectMapper objectMapper;

   public FiasTcpClient(ObjectMapper objectMapper)
   {
      this.objectMapper = objectMapper;
   }

   public List<String> fetchRoomStatus()
   {
      List<String> events = new ArrayList<>();

      try (Socket socket = new Socket())
      {
         socket.connect(new InetSocketAddress(HOST, PORT), CONNECTION_TIMEOUT_MS);

         log.info("Connected to FIAS simulator {}:{}", HOST, PORT);

         try (BufferedReader reader = new BufferedReader(
                  new InputStreamReader(socket.getInputStream()));

                  PrintWriter writer = new PrintWriter(socket.getOutputStream(), true))
         {
            FiasRequest request = new FiasRequest("ROOM_STATUS");

            String requestJson = objectMapper.writeValueAsString(request);

            writer.println(requestJson);

            log.info("FIAS request sent: {}", requestJson);

            String event;

            while ((event = reader.readLine()) != null)
            {
               if (!event.isBlank())
               {
                  events.add(event);
               }
            }
         }

         log.info("Received {} FIAS events", events.size());
      }
      catch (Exception e)
      {
         log.error("Failed to fetch room status from FIAS", e);
      }

      return events;
   }
}
