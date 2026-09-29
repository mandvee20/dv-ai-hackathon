package com.digivalet.core.tcp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.digivalet.core.controller.IntentProcessor;
import com.digivalet.core.model.IntentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class TcpClientHandler
{
   private static final Logger log = LoggerFactory.getLogger(TcpClientHandler.class);

   private final ObjectMapper objectMapper;

   private final IntentProcessor intentProcessor;

   public TcpClientHandler(ObjectMapper objectMapper, IntentProcessor intentProcessor)
   {
      this.objectMapper = objectMapper;
      this.intentProcessor = intentProcessor;
   }

   public void handle(Socket socket)
   {
      try (socket;
               BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

               PrintWriter writer = new PrintWriter(socket.getOutputStream(), true))
      {
         String message;

         while ((message = reader.readLine()) != null)
         {
            log.info("Received TCP message: {}", message);

            try
            {
               IntentRequest request = objectMapper.readValue(message, IntentRequest.class);

               intentProcessor.process(request);

               writer.println("""
                        {"status":"RECEIVED"}
                        """);
            }
            catch (Exception e)
            {
               log.error("Unable to process TCP message", e);

               writer.println("""
                        {"status":"FAILED"}
                        """);
            }
         }
      }
      catch (Exception e)
      {
         log.error("TCP client connection failed", e);
      }
   }
}
