package com.wakana.vefa.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RealEstatePropertyKpiResponse {
    private Long propertyId;
    private int totalWorks;
    private double totalCharges;
    private double totalCollected;
    private double totalPending;
}
