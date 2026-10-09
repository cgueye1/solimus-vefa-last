package com.wakana.vefa.dto.response;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WorkKpiResponse {
    private Long workId;
    private int totalInvoices;       // nombre de factures
    private double totalCharges;     // somme totale des frais
    private double totalPaid;        // montant payé
    private double totalPending;     // montant en attente
}
