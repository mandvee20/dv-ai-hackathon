package com.digivalet.agent.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author Mandvee Vatsa
 * @date Sep 29, 2026
 */
@Data
public class PreCheckInRequest
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
   
   private String roomNumber;
}
