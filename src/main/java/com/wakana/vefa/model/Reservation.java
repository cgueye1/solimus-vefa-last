package com.wakana.vefa.model;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

import com.wakana.vefa.enums.ReservationStatus;
@Data
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reservataire_id", nullable = false)
    private User user;  

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private RealEstateProperty property; 

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;  
    
    private Long createdAt;
    private LocalDateTime validatedAt;
    
  
}
