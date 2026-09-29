package com.precheckin.simulator.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreCheckinApiRequest
{
    private String reservationNumber;

    private String firstName;

    private String lastName;

    private String email;

    private String mobileNumber;

    private String arrivalDate;

    private String departureDate;

    private String estimatedArrivalTime;

    private String nationality;

    private String idType;

    private String idNumber;

    private String roomNumber;

    // getters/setters
}
