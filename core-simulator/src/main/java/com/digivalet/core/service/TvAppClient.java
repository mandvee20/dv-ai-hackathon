package com.digivalet.core.service;

import com.digivalet.core.model.IntentRequest;
import com.digivalet.core.model.MovieRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class TvAppClient
{
   private final RestClient restClient;

   public TvAppClient(@Value("${tv-app.url:http://localhost:8082}") String tvAppUrl)
   {
      this.restClient = RestClient.builder().baseUrl(tvAppUrl).build();
   }

   public void playMovie(IntentRequest request, String movieUrl)
   {
      String movieId = String.valueOf(request.getParameters().get("movieId"));

      String movieName = String.valueOf(request.getParameters().get("movieName"));

      MovieRequest movieRequest =
               new MovieRequest(request.getRequestId(), request.getRoomId(), movieId, movieName);

      log.info("Sending movie command to TV App. requestId={}, roomId={}, movieId={}",
               request.getRequestId(), request.getRoomId(), movieId);

      try
      {
         restClient.post().uri("/tv/play").contentType(MediaType.APPLICATION_JSON)
                  .body(new TvMovieCommand(movieRequest.getRequestId(), movieRequest.getRoomId(),
                           movieRequest.getMovieId(), movieRequest.getMovieName(), movieUrl))
                  .retrieve().toBodilessEntity();

         log.info("Movie command successfully sent to TV App. requestId={}, roomId={}",
                  request.getRequestId(), request.getRoomId());
      }
      catch (Exception e)
      {
         log.error("Failed to send movie command to TV App. requestId={}, roomId={}",
                  request.getRequestId(), request.getRoomId(), e);

         throw new RuntimeException("Failed to send movie command to TV App", e);
      }
   }

   private record TvMovieCommand(String requestId, String roomId, String movieId, String movieName,
                                 String movieUrl)
   {
   }
}
