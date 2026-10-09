package com.wakana.vefa.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentCallKpiDto {
    private double totalAmount;      // Prix total du bien
    private double paidAmount;       // Montant déjà payé (tous appels >0)
    private double remainingAmount;  // Montant restant
    private double remainingPercentage; // Pourcentage restant
}