package com.wakana.vefa.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "property_types")
public class PropertyType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String typeName; // Name of the property type (e.g., apartment, house, commercial space)
    @JsonIgnore
    @OneToMany(mappedBy = "propertyType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RealEstateProperty> realEstateProperties; // List of properties associated with this type
    private boolean isParent;
}
