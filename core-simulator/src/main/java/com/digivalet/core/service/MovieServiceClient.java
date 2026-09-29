package com.digivalet.core.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.digivalet.core.model.IntentRequest;

@Service
public class MovieServiceClient
{
   private final RestClient restClient;

   public MovieServiceClient(RestClient.Builder builder)
   {
      this.restClient = builder.baseUrl("http://localhost:9090").build();
   }

   public String generateMovieUrl(IntentRequest request)
   {
      Map<?, ?> response =
               restClient.post().uri("/movie/play").body(request).retrieve().body(Map.class);

      if (response == null || response.get("movieUrl") == null)
      {
         throw new IllegalStateException("Movie URL was not generated");
      }

      return response.get("movieUrl").toString();
   }
}
