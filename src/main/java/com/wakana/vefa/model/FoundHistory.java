package com.wakana.vefa.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "founds_histories")
public class FoundHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private int type;
    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "rental_invoice_id", nullable = true)
    private RentalInvoice rentalInvoice;

    private double amount;
    private boolean payed;
    private LocalDateTime lastUpdated;

}
