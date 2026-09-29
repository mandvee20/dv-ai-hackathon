package com.digivalet.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.digivalet.core.model.IntentRequest;
import com.digivalet.core.model.IntentType;

@Service
public class CoreIntentService
{
   private static final Logger log = LoggerFactory.getLogger(CoreIntentService.class);

   private final MovieServiceClient movieServiceClient;

   private final RmsService rmsService;

   private final FailureEventPublisher failureEventPublisher;

   public CoreIntentService(MovieServiceClient movieServiceClient, RmsService rmsService,
            FailureEventPublisher failureEventPublisher)
   {
      this.movieServiceClient = movieServiceClient;
      this.rmsService = rmsService;
      this.failureEventPublisher = failureEventPublisher;
   }

   public void process(IntentRequest request)
   {
      try
      {
         switch (request.getIntent())
         {
            case PLAY_MOVIE -> processMovie(request);

            case LIGHT_ON, LIGHT_OFF, CURTAIN_OPEN, CURTAIN_CLOSE -> processRmsOperation(request);

            default -> throw new IllegalArgumentException(
                     "Unsupported intent: " + request.getIntent());
         }
      }
      catch (Exception e)
      {
         log.error("Intent failed requestId={} roomId={} intent={}", request.getRequestId(),
                  request.getRoomId(), request.getIntent(), e);

         failureEventPublisher.publish(request, e.getMessage());
      }
   }

   private void processMovie(IntentRequest request)
   {
      log.info("Calling Movie Service requestId={} roomId={}", request.getRequestId(),
               request.getRoomId());

      String movieUrl = movieServiceClient.generateMovieUrl(request);

      log.info("Movie URL received requestId={} roomId={}", request.getRequestId(),
               request.getRoomId());

      // In the next step this will call TV App.
      log.info("Sending movie play command to TV App requestId={} roomId={} url={}",
               request.getRequestId(), request.getRoomId(), movieUrl);
   }

   private void processRmsOperation(IntentRequest request)
   {
      log.info("Sending RMS command requestId={} roomId={} intent={}", request.getRequestId(),
               request.getRoomId(), request.getIntent());

      rmsService.execute(request);
   }
}
