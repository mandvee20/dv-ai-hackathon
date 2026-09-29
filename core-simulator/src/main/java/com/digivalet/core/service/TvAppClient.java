package com.digivalet.core.service;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.digivalet.core.model.IntentRequest;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TvAppClient
{
   private final AtomicInteger requestCounter = new AtomicInteger();

   public void playMovie(IntentRequest request, String movieUrl)
   {
      String movieId = String.valueOf(request.getParameters().get("movieId"));

      String movieName = String.valueOf(request.getParameters().get("movieName"));

      int count = requestCounter.incrementAndGet();

      log.info("Sending movie command to TV App. requestId={}, roomId={}, movieId={}, movieName={}, count={}",
               request.getRequestId(), request.getRoomId(), movieId, movieName, count);

      // Alternate between successful and failed TV playback.
      if (count % 2 == 0)
      {
         String error = "TV is not playing the movie";

         log.error("TV App failed to play movie. requestId={}, roomId={}, movieId={}, error={}",
                  request.getRequestId(), request.getRoomId(), movieId, error);

         throw new RuntimeException(error);
      }

      log.info("TV App started playing movie. requestId={}, roomId={}, movieId={}, movieUrl={}",
               request.getRequestId(), request.getRoomId(), movieId, movieUrl);
   }

   public void execute(IntentRequest request)
   {
      String deviceId = String.valueOf(request.getParameters().get("deviceId"));

      String command = request.getIntent().name();

      log.info("Processing TV command requestId={} roomId={} deviceId={} command={}",
               request.getRequestId(), request.getRoomId(), deviceId, command);

      switch (command)
      {
         case "TV_ON" -> log.info("TV turned ON requestId={} roomId={} deviceId={}",
                  request.getRequestId(), request.getRoomId(), deviceId);

         case "TV_OFF" -> log.info("TV turned OFF requestId={} roomId={} deviceId={}",
                  request.getRequestId(), request.getRoomId(), deviceId);

         default -> throw new IllegalArgumentException("Unsupported TV command: " + command);
      }
   }
}
