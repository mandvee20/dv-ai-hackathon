package com.digivalet.movie.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.digivalet.movie.model.MovieRequest;
import com.digivalet.movie.model.MovieResponse;
import com.digivalet.movie.service.MovieService;

@RestController
@RequestMapping("/movie")
public class MovieController
{
   private final MovieService movieService;

   public MovieController(MovieService movieService)
   {
      this.movieService = movieService;
   }

   @PostMapping("/play")
   public ResponseEntity<MovieResponse> play(@RequestBody MovieRequest request)
   {
      return ResponseEntity.ok(movieService.generateMovieUrl(request));
   }
}
