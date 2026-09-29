package com.digivalet.agent.service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import com.digivalet.agent.entity.FailureAnalysis;
import com.digivalet.agent.model.FailureEvent;
import com.digivalet.agent.repository.FailureAnalysisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class FailureAnalysisService
{
   private final FailureAnalysisRepository repository;

   @Autowired
   private MqttService mqttService;

   public FailureAnalysisService(
            FailureAnalysisRepository repository)
   {
      this.repository = repository;
   }

   public void saveFailure(FailureEvent event)
   {
      String logs = event.getLogs() == null
               ? ""
               : event.getLogs()
                        .stream()
                        .collect(Collectors.joining("\n"));

      FailureAnalysis failureAnalysis = new FailureAnalysis();

      failureAnalysis.setRequestId(event.getRequestId());
      failureAnalysis.setRoomId(event.getRoomId());
      failureAnalysis.setIntent(event.getIntent());
      failureAnalysis.setTimestamp(LocalDateTime.now());
      failureAnalysis.setError(event.getError());
      failureAnalysis.setLogs(logs);

      repository.save(failureAnalysis);

      mqttService.publishFailureEvent(event);
   }
}
