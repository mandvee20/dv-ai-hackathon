package com.precheckin.simulator.service.impl;

import com.precheckin.simulator.dto.PreCheckinRequest;
import com.precheckin.simulator.entity.PreCheckin;
import com.precheckin.simulator.repository.PreCheckinRepository;
import com.precheckin.simulator.service.PreCheckinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class PreCheckinServiceImpl implements PreCheckinService
{
    private final PreCheckinRepository preCheckinRepository;

    public PreCheckinServiceImpl(
            PreCheckinRepository preCheckinRepository)
    {
        this.preCheckinRepository = preCheckinRepository;
    }

    @Override
    public PreCheckin createPreCheckin(PreCheckinRequest request)
    {
        log.info("Starting pre-check-in processing for reservation: {}",
                 request.getReservationNumber());
        PreCheckin preCheckin = new PreCheckin();
        try
        {
            preCheckin.setReservationNumber(request.getReservationNumber());

            preCheckin.setFirstName(request.getFirstName());

            preCheckin.setLastName(request.getLastName());

            preCheckin.setEmail(request.getEmail());

            preCheckin.setMobileNumber(request.getMobileNumber());

            preCheckin.setArrivalDate(request.getArrivalDate());

            preCheckin.setDepartureDate(request.getDepartureDate());

            preCheckin.setEstimatedArrivalTime(request.getEstimatedArrivalTime());

            preCheckin.setNationality(request.getNationality());

            preCheckin.setIdType(request.getIdType());

            preCheckin.setIdNumber(request.getIdNumber());

            preCheckin.setStatus("RECEIVED");

            preCheckin.setCreatedAt(LocalDateTime.now());

            preCheckin.setUpdatedAt(LocalDateTime.now());

            log.debug("Saving pre-check-in data for reservation: {}",
                     request.getReservationNumber());

            PreCheckin savedPreCheckin = preCheckinRepository.save(preCheckin);

            log.info("Pre-check-in data saved successfully. Registration ID: {}, Reservation: {}",
                     savedPreCheckin.getId(), savedPreCheckin.getReservationNumber());

            return savedPreCheckin;
        }
        catch (Exception e)
        {
            log.error("Error while creating pre checkin", e);
        }
        return preCheckin;
    }
}
