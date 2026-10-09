package com.wakana.vefa.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PaymentHistoryResponse {

    private String nom;
    private String prenom;
    private String planLabel;
    private double amount;
    private boolean paid;
    private LocalDateTime date;
    private String number;
    private Long userId;
    private Long planId;
}
