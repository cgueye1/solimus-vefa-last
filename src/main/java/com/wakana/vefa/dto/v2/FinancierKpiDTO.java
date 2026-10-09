package com.wakana.vefa.dto.v2;
import lombok.Data;

@Data
public class FinancierKpiDTO {
    // Direction Générale - Financier
    private double chiffreAffairesSigne;     // somme prix lots SOLD
    private double totalEncaissementsRecus;  // PaymentCall PAID
    private double totalEncaissementsPrevus; // tous PaymentCall
    private double resteAEncaisser;
    private double tauxRecouvrement;         // reçus / prévus * 100

    // Financier & Encaissements - Clients
    private long clientsAJour;
    private long clientsEnRetard;
    private long clientsArisque;
}