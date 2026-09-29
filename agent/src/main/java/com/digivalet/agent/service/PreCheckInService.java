package com.digivalet.agent.service;

import org.springframework.stereotype.Service;
import com.digivalet.agent.dto.PreCheckInRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Mandvee Vatsa
 * @date Sep 29, 2026
 */
@Service
@Slf4j
public class PreCheckInService
{
   public void processPreCheckIn(PreCheckInRequest request) {

      log.info(
              "Received pre-check-in request for reservationNumber={}",
              request.getReservationNumber()
      );

      // Phase 1:
      // Store event / create validation run.
      //
      // Phase 2:
      // Load applicable intents.
      //
      // Phase 3:
      // Start room validation.
  }
}
