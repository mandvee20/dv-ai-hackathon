package com.precheckin.simulator.controller;

import com.precheckin.simulator.dto.PreCheckinRequest;
import com.precheckin.simulator.entity.PreCheckin;
import com.precheckin.simulator.service.PreCheckinService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pre-checkin")
@Slf4j
public class PreCheckinController
{
    private final PreCheckinService preCheckinService;

    public PreCheckinController(
            PreCheckinService preCheckinService)
    {
        this.preCheckinService = preCheckinService;
    }

    @PostMapping
    public ResponseEntity<PreCheckin> createPreCheckin(
             @Valid @RequestBody PreCheckinRequest request)
    {
        log.info("Received pre-check-in request for reservation: {}",
                 request.getReservationNumber());
        try
        {
            PreCheckin response = preCheckinService.createPreCheckin(request);
            log.info("Pre-check-in created successfully. Registration ID: {}, Reservation: {}",
                     response.getId(), response.getReservationNumber());

            return ResponseEntity.ok(response);
        }
        catch (Exception e)
        {
            log.error("Error while processing pre-check-in request for reservation: {}",
                     request.getReservationNumber(), e);

            throw e;
        }
    }
}
