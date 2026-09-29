package com.fias.simulator;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

@Component
public class FiasTcpServer
{
   private static final Logger log = LoggerFactory.getLogger(FiasTcpServer.class);

   private static final int PORT = 9000;

   private final ObjectMapper objectMapper;

   private final FiasResponseGenerator responseGenerator;

   private volatile boolean running = true;

   private ServerSocket serverSocket;

   public FiasTcpServer(ObjectMapper objectMapper, FiasResponseGenerator responseGenerator)
   {
      this.objectMapper = objectMapper;
      this.responseGenerator = responseGenerator;
   }

   @PostConstruct
   public void start()
   {
      Thread.ofVirtual().name("fias-tcp-server").start(this::startServer);
   }

   private void startServer()
   {
      try
      {
         serverSocket = new ServerSocket(PORT);

         log.info("FIAS TCP server started on port {}", PORT);

         while (running)
         {
            Socket socket = serverSocket.accept();

            log.info("Client connected: {}", socket.getRemoteSocketAddress());

            Thread.ofVirtual().start(() -> handleClient(socket));
         }
      }
      catch (Exception e)
      {
         if (running)
         {
            log.error("FIAS TCP server failed", e);
         }
      }
   }

   private void handleClient(Socket socket)
   {
      try (socket;

               BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

               PrintWriter writer = new PrintWriter(socket.getOutputStream(), true))
      {
         String requestJson = reader.readLine();

         log.info("Request received: {}", requestJson);

         FiasRequest request = objectMapper.readValue(requestJson, FiasRequest.class);

         if (!"ROOM_STATUS".equalsIgnoreCase(request.getRequest()))
         {
            writer.println("{\"error\":\"Unsupported request\"}");

            return;
         }

         sendRoomStatusEvents(writer);
      }
      catch (Exception e)
      {
         log.error("Error processing FIAS request", e);
      }
   }

   private void sendRoomStatusEvents(PrintWriter writer)
   {
      List<String> events = responseGenerator.generateRoomStatusEvents();

      log.info("Sending {} FIAS room status events", events.size());

      for (String event : events)
      {
         writer.println(event);

         log.info("FIAS event sent: {}", event);
      }
   }

   @PreDestroy
   public void stop()
   {
      running = false;

      try
      {
         if (serverSocket != null)
         {
            serverSocket.close();
         }
      }
      catch (Exception e)
      {
         log.warn("Error closing FIAS TCP server", e);
      }
   }
}
