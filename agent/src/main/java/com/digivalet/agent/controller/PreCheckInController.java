package com.digivalet.agent.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.digivalet.agent.dto.PreCheckInRequest;
import com.digivalet.agent.service.PreCheckInService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Mandvee Vatsa
 * @date Sep 29, 2026
 */
@RestController
@RequestMapping("/api/v1/pre-checkin")
@RequiredArgsConstructor
@Slf4j
public class PreCheckInController
{

   private final PreCheckInService preCheckInService;

   @PostMapping
   public ResponseEntity<Void> receivePreCheckIn(@Valid @RequestBody PreCheckInRequest request)
   {

      log.info("Pre-check-in event received | reservationNumber={} | firstName={} | lastName={} | arrivalDate={} | departureDate={}",
               request.getReservationNumber(), request.getFirstName(), request.getLastName(),
               request.getArrivalDate(), request.getDepartureDate());
      preCheckInService.processPreCheckIn(request);

      return ResponseEntity.accepted().build();
   }
}
