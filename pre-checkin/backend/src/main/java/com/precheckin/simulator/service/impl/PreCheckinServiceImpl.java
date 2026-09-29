package com.precheckin.simulator.service.impl;

import com.precheckin.simulator.apiclient.PreCheckinApiClient;
import com.precheckin.simulator.dto.PreCheckinApiRequest;
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
    private final PreCheckinApiClient preCheckinApiClient;

    public PreCheckinServiceImpl(
             PreCheckinRepository preCheckinRepository,
             PreCheckinApiClient preCheckinApiClient)
    {
        this.preCheckinRepository = preCheckinRepository;
        this.preCheckinApiClient = preCheckinApiClient;
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

            preCheckin.setRoomNumber(request.getRoomNumber());

            log.debug("Saving pre-check-in data for reservation: {}",
                     request.getReservationNumber());

            PreCheckin savedPreCheckin = preCheckinRepository.save(preCheckin);

            log.info(
                     "Pre-check-in data saved successfully. Registration ID: {}, Reservation: {}",
                     savedPreCheckin.getId(),
                     savedPreCheckin.getReservationNumber());

            PreCheckinApiRequest apiRequest =
                     new PreCheckinApiRequest();

            apiRequest.setReservationNumber(
                     savedPreCheckin.getReservationNumber());

            apiRequest.setFirstName(
                     savedPreCheckin.getFirstName());

            apiRequest.setLastName(
                     savedPreCheckin.getLastName());

            apiRequest.setEmail(
                     savedPreCheckin.getEmail());

            apiRequest.setMobileNumber(
                     savedPreCheckin.getMobileNumber());

            apiRequest.setArrivalDate(
                     String.valueOf(savedPreCheckin.getArrivalDate()));

            apiRequest.setDepartureDate(
                     String.valueOf(savedPreCheckin.getDepartureDate()));

            apiRequest.setEstimatedArrivalTime(
                     savedPreCheckin.getEstimatedArrivalTime());

            apiRequest.setNationality(
                     savedPreCheckin.getNationality());

            apiRequest.setIdType(
                     savedPreCheckin.getIdType());

            apiRequest.setIdNumber(
                     savedPreCheckin.getIdNumber());

            apiRequest.setRoomNumber(
                     savedPreCheckin.getRoomNumber());

            log.info(
                     "Sending pre-check-in data to external service. Reservation: {}, Room: {}",
                     savedPreCheckin.getReservationNumber(),
                     savedPreCheckin.getRoomNumber());

            preCheckinApiClient.sendPreCheckin(apiRequest);

            return savedPreCheckin;
        }
        catch (Exception e)
        {
            log.error("Error while creating pre checkin", e);
        }
        return preCheckin;
    }
}
