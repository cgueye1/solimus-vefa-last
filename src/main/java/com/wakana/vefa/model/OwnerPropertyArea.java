package com.wakana.vefa.model;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "owner_property_area ")
public class OwnerPropertyArea  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "real_estate_property_id", nullable = false)
    private RealEstateProperty realEstateProperty;
    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_owner_id", nullable = false)
    private User owner; 
    private double areaInSquareMeters;

    
}

