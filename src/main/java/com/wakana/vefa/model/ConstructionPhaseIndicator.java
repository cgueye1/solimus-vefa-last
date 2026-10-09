package com.wakana.vefa.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@Entity
@Table(name = "construction_phase_indicators")
public class ConstructionPhaseIndicator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "real_estate_property_id", nullable = false)
    private RealEstateProperty realEstateProperty;

    private String phaseName; // Phase names: "Gros œuvre", "Second œuvre", "Finition"

    private int progressPercentage = 0; // Initial progress is 0%
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastUpdated; // Timestamp for tracking updates

    /*@PrePersist
    @PreUpdate
    public void setLastUpdated() {
        this.lastUpdated = LocalDateTime.now(); // Automatically set the update time
    }*/

    public ConstructionPhaseIndicator() {}

    public ConstructionPhaseIndicator(String phaseName, RealEstateProperty realEstateProperty) {
        this.phaseName = phaseName;
        this.realEstateProperty = realEstateProperty;
        this.progressPercentage = 0; // Default percentage
        this.lastUpdated = LocalDateTime.now(); // Initial timestamp
    }
}
