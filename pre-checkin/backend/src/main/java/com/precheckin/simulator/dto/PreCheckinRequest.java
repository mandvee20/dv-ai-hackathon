package com.precheckin.simulator.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PreCheckinRequest
{
    @NotBlank
    private String reservationNumber;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String mobileNumber;

    private LocalDate arrivalDate;

    private LocalDate departureDate;

    private String estimatedArrivalTime;

    private String nationality;

    private String idType;

    private String idNumber;

    // getters and setters
}
