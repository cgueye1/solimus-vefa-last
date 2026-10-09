package com.wakana.vefa.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "rental_contracts")
public class RentalContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Bien loué
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private RealEstateProperty property;

    // Locataire (recipient)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    // Dates
    private LocalDate startDate;
    private LocalDate endDate;

    // Fichier du contrat (chemin serveur)
    private String contractFile;

    // Statuts
    private boolean archived;
    private boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime archivedAt;
}
