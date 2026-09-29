package com.digivalet.core.service;

import java.time.Instant;

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

   public FailureEventPublisher(
            @Value("${log-analysis-service.url:http://localhost:8084}") String serviceUrl)
   {
      this.restClient = RestClient.builder().baseUrl(serviceUrl).build();
   }

   public void publish(IntentRequest request, String error)
   {
      try
      {
         FailureEvent event = new FailureEvent(request.getRequestId(), request.getRoomId(),
                  request.getIntent().name(), Instant.now().toString(), error);

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
}
