package com.wakana.vefa.dto;
import lombok.Data;


@Data
public class DashboardStats {
    private final long nombreDeBien;
    private final long nombreDeLotsVendus;
    private final long nombreDeLotsDisponible;
    private final long nombreDeLotsReserve;
    
    public DashboardStats(long nombreDeBien, long nombreDeLotsVendus, long nombreDeLotsDisponible, long nombreDeLotsReserve) {
        this.nombreDeBien = nombreDeBien;
        this.nombreDeLotsVendus = nombreDeLotsVendus;
        this.nombreDeLotsDisponible = nombreDeLotsDisponible;
        this.nombreDeLotsReserve = nombreDeLotsReserve;
    }

}
