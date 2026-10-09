package com.wakana.vefa.dto.v2;
import lombok.Data;

@Data
public class CommercialKpiDTO {
    // Direction Générale - Commercial
    private int totalLots;
    private int lotsVendus;
    private int lotsReserves;
    private int lotsDisponibles;
    private double tauxCommercialisation; // % (vendus + réservés) / total
    private double vitesseVente; // lots vendus par mois

    // Direction Commerciale - Performances produits
    private double prixMoyenVente;
    private java.util.Map<String, Long> repartitionParNombreChambres; // F2, F3, F4...
}