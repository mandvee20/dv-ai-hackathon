package com.digivalet.movie.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponse
{
   private String requestId;

   private String roomId;

   private String movieUrl;
}
