package com.digivalet.agent.entity;

import java.time.LocalDateTime;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "failure_analysis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FailureAnalysis
{
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(nullable = false)
   private String requestId;

   private String roomId;

   private String intent;

   private LocalDateTime timestamp;

   @Column(length = 2000)
   private String error;

   @Column(columnDefinition = "TEXT")
   private String logs;
}
