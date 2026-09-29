package com.precheckin.simulator.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pre_checkin")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreCheckin
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_number", nullable = false)
    private String reservationNumber;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "mobile_number", nullable = false)
    private String mobileNumber;

    private LocalDate arrivalDate;

    private LocalDate departureDate;

    private String estimatedArrivalTime;

    private String nationality;

    private String idType;

    private String idNumber;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String roomNumber;
}
