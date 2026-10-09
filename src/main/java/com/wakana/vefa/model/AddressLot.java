package com.wakana.vefa.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class AddressLot {

    private Long propertyId;
    private String lot;
    private double area;
}
