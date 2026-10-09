package com.wakana.vefa.dto;

import lombok.Data;

@Data
public class ReservationRequest {
    private long userId;
    private long propertyId;
    private String prenom;
    private String nom;
    private String email;
    private String telephone;
    private boolean isManifest;

    private boolean tenant;
}
