package com.digivalet.movie.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieRequest
{
   private String requestId;

   private String roomId;

   private String movieId;

   private String movieName;
}
