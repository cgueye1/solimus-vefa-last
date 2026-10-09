package com.wakana.vefa.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "rental_invoice")
public class  RentalInvoice {
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private RealEstateProperty property;

    private double amount; // Montant à payer

    private boolean paid; // Statut de paiement

    private LocalDateTime paymentDate; // Date de paiement

    private LocalDateTime dueDate; // Date d'échéance

}
