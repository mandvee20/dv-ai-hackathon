package com.digivalet.movie.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.digivalet.movie.model.FailureEvent;
import com.digivalet.movie.model.MovieRequest;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FailureEventPublisher
{
   private final RestClient restClient;

   private final Path logFile;

   public FailureEventPublisher(
            @Value("${log-analysis-service.url:http://localhost:8084}") String serviceUrl,
            @Value("${logging.file.name:logs/movie-service.log}") String logFilePath)
   {
      this.restClient = RestClient.builder().baseUrl(serviceUrl).build();

      this.logFile = Path.of(logFilePath);
   }

   public void publish(MovieRequest request, String error)
   {
      try
      {
         List<String> requestLogs = getRequestLogs(request.getRequestId());

         FailureEvent event =
                  new FailureEvent(request.getRequestId(), request.getRoomId(), "PLAY_MOVIE",
                           Instant.now().toString(), error, requestLogs);

         restClient.post().uri("/analysis/failure").contentType(MediaType.APPLICATION_JSON)
                  .body(event).retrieve().toBodilessEntity();

         log.info("Failure logs sent to Log Analysis Service requestId={} logCount={}",
                  request.getRequestId(), requestLogs.size());
      }
      catch (Exception e)
      {
         log.error("Failed to send failure logs requestId={}", request.getRequestId(), e);
      }
   }

   private List<String> getRequestLogs(String requestId)
   {
      try
      {
         if (!Files.exists(logFile))
         {
            log.warn("Movie Service log file does not exist path={}", logFile);

            return List.of();
         }

         return Files.readAllLines(logFile).stream().filter(line -> line.contains(requestId))
                  .toList();
      }
      catch (Exception e)
      {
         log.error("Failed to read Movie Service log file requestId={}", requestId, e);

         return List.of();
      }
   }
}
