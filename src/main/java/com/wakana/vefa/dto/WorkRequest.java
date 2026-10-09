package com.wakana.vefa.dto;

import lombok.Data;

@Data
public class WorkRequest {
    private String titre;
    private String description;
    private double price;

    // Dates en texte format "dd-MM-yyyy"
    private String startDate;
    private String deliveryDate;

    private boolean generateCallForCharge;
}
