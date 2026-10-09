package com.wakana.vefa.dto.v2;

import lombok.Data;
import com.wakana.vefa.enums.PaymentCallStatusEnum;

@Data
public class EncaissementDetailDTO {
    private Long lotId;
    private String lotName;
    private String lotNumber;
    private String clientNom;
    private String clientPrenom;
    private String label;
    private String ref;
    private double montantPrevu;
    private double montantRecu;
    private String dateAttendue;
    private PaymentCallStatusEnum status;
}