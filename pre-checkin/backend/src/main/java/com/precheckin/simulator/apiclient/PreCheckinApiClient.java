package com.precheckin.simulator.apiclient;

import com.precheckin.simulator.dto.PreCheckinApiRequest;
import org.springframework.web.client.RestClient;
import org.springframework.stereotype.Component;

@Component
public class PreCheckinApiClient
{
   private final RestClient restClient;

   public PreCheckinApiClient(RestClient.Builder restClientBuilder)
   {
      this.restClient = restClientBuilder.baseUrl("http://localhost:9079").build();
   }

   public void sendPreCheckin(PreCheckinApiRequest request)
   {
      restClient.post().uri("/api/v1/pre-checkin").body(request).retrieve().toBodilessEntity();
   }
}
