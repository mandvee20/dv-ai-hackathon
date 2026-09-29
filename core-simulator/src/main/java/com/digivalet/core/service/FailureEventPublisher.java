package com.digivalet.core.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.digivalet.core.model.FailureEvent;
import com.digivalet.core.model.IntentRequest;

@Service
@Slf4j
public class FailureEventPublisher
{
   private final RestClient restClient;
   private final Path logFile;

   public FailureEventPublisher(
            @Value("${log-analysis-service.url:http://localhost:8084}") String serviceUrl,
            @Value("${logging.file.name:logs/core-simulator.log}") String logFilePath)
   {
      this.restClient = RestClient.builder().baseUrl(serviceUrl).build();

      this.logFile = Path.of(logFilePath);
   }

   public void publish(IntentRequest request, String error)
   {
      try
      {
         List<String> requestLogs = getRequestLogs(request.getRequestId());

         FailureEvent event = new FailureEvent(request.getRequestId(), request.getRoomId(),
                  request.getIntent().name(), Instant.now().toString(), error, requestLogs);

         restClient.post().uri("/analysis/failure").contentType(MediaType.APPLICATION_JSON)
                  .body(event).retrieve().toBodilessEntity();

         log.info("Failure event sent to Log Analysis Service requestId={} roomId={} intent={}",
                  request.getRequestId(), request.getRoomId(), request.getIntent());
      }
      catch (Exception e)
      {
         log.error("Failed to send failure event requestId={} roomId={} intent={}",
                  request.getRequestId(), request.getRoomId(), request.getIntent(), e);

         throw new IllegalStateException("Unable to send failure event to Log Analysis Service", e);
      }
   }

   private List<String> getRequestLogs(String requestId)
   {
      try
      {
         if (!Files.exists(logFile))
         {
            log.warn("Core log file does not exist path={}", logFile);

            return List.of();
         }

         return Files.readAllLines(logFile).stream().filter(line -> line.contains(requestId))
                  .toList();
      }
      catch (Exception e)
      {
         log.error("Failed to read Core log file requestId={}", requestId, e);

         return List.of();
      }
   }
}
