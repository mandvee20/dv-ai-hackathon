package com.digivalet.agent.controller;

import com.digivalet.agent.model.FailureEvent;
import com.digivalet.agent.service.FailureAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/analysis")
public class FailureAnalysisController
{
   private final FailureAnalysisService failureAnalysisService;

   public FailureAnalysisController(FailureAnalysisService failureAnalysisService)
   {
      this.failureAnalysisService = failureAnalysisService;
   }

   @PostMapping("/failure")
   public ResponseEntity<Void> receiveFailure(@RequestBody FailureEvent event)
   {
      failureAnalysisService.saveFailure(event);

      return ResponseEntity.ok().build();
   }
}
