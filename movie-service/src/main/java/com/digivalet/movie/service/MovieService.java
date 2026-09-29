package com.digivalet.movie.service;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.digivalet.movie.model.MovieRequest;
import com.digivalet.movie.model.MovieResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MovieService
{
   private final AtomicInteger requestCounter = new AtomicInteger();

   private final FailureEventPublisher failureEventPublisher;

   public MovieService(FailureEventPublisher failureEventPublisher)
   {
      this.failureEventPublisher = failureEventPublisher;
   }

   public MovieResponse generateMovieUrl(MovieRequest request)
   {
      int count = requestCounter.incrementAndGet();

      log.info("Processing movie request requestId={} roomId={} movieId={} count={}",
               request.getRequestId(), request.getRoomId(), request.getMovieId(), count);

      if (count % 2 == 0)
      {
         String error = "Movie URL not available";

         log.error("Movie URL generation failed requestId={} roomId={} movieId={} error={}",
                  request.getRequestId(), request.getRoomId(), request.getMovieId(), error);

         failureEventPublisher.publish(request, error);

         return new MovieResponse(request.getRequestId(), request.getRoomId(), null);
      }

      String movieUrl =
               "http://movie-server/movies/" + request.getMovieId() + "-" + UUID.randomUUID() + ".mp4";

      log.info("Movie URL generated requestId={} roomId={} movieId={}", request.getRequestId(),
               request.getRoomId(), request.getMovieId());

      return new MovieResponse(request.getRequestId(), request.getRoomId(), movieUrl);
   }
}
