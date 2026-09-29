package com.digivalet.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.digivalet.core.model.IntentRequest;
import com.digivalet.core.model.IntentType;

@Service
public class CoreIntentService
{
   private static final Logger log = LoggerFactory.getLogger(CoreIntentService.class);

   private final MovieServiceClient movieServiceClient;

   private final RmsDeviceClient rmsDeviceClient;

   private final FailureEventPublisher failureEventPublisher;

   @Autowired
   private TvAppClient tvAppClient;

   public CoreIntentService(MovieServiceClient movieServiceClient, RmsDeviceClient rmsDeviceClient,
            FailureEventPublisher failureEventPublisher)
   {
      this.movieServiceClient = movieServiceClient;
      this.rmsDeviceClient = rmsDeviceClient;
      this.failureEventPublisher = failureEventPublisher;
   }

   public void process(IntentRequest request)
   {
      try
      {
         switch (request.getIntent())
         {
            case PLAY_MOVIE -> processMovie(request);

            case TV_ON, TV_OFF -> processTvOperation(request);

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

      if (movieUrl == null || movieUrl.isBlank())
      {
         throw new IllegalStateException("Movie URL was not generated");
      }

      log.info("Sending movie play command to TV App requestId={} roomId={}",
               request.getRequestId(), request.getRoomId());

      tvAppClient.playMovie(request, movieUrl);

      log.info("Movie play command completed requestId={} roomId={}", request.getRequestId(),
               request.getRoomId());
   }

   private void processTvOperation(IntentRequest request)
   {
      log.info(
               "Sending TV command requestId={} roomId={} intent={}",
               request.getRequestId(),
               request.getRoomId(),
               request.getIntent());

      tvAppClient.execute(request);

      log.info(
               "TV command completed requestId={} roomId={} intent={}",
               request.getRequestId(),
               request.getRoomId(),
               request.getIntent());
   }

   private void processRmsOperation(IntentRequest request)
   {
      log.info("Sending RMS command requestId={} roomId={} intent={}", request.getRequestId(),
               request.getRoomId(), request.getIntent());

      rmsDeviceClient.execute(request);
   }
}
