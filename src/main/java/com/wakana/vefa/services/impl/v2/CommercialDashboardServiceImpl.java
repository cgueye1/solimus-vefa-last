package com.wakana.vefa.services.impl.v2;
// CommercialDashboardServiceImpl.java

import com.wakana.vefa.dto.v2.CommercialKpiDTO;
import com.wakana.vefa.enums.RealEstatePropertyStatusEnum;
import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.repository.LotDashboardRepository;
import com.wakana.vefa.services.V2.CommercialDashboardService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommercialDashboardServiceImpl implements CommercialDashboardService {

    private final LotDashboardRepository lotRepo;

    @Override
    public CommercialKpiDTO getCommercialKpi(Long projectId) {
        List<RealEstateProperty> allLots = lotRepo.findLotsByProjectId(projectId);

        long totalLots = allLots.size();
        long lotsVendus = allLots.stream().filter(l -> l.getStatus() == RealEstatePropertyStatusEnum.SOLD).count();
        long lotsReserves = allLots.stream().filter(l -> l.getStatus() == RealEstatePropertyStatusEnum.RESERVED)
                .count();
        long lotsDisponibles = allLots.stream().filter(l -> l.getStatus() == RealEstatePropertyStatusEnum.AVAILABLE)
                .count();

        double tauxCommercialisation = totalLots > 0
                ? ((lotsVendus + lotsReserves) * 100.0) / totalLots
                : 0;

        // Vitesse de vente : lots vendus / nombre de mois depuis le plus ancien lot
        // vendu
        double vitesseVente = calculerVitesseVente(allLots);

        // Prix moyen des lots vendus
        double prixMoyen = allLots.stream()
                .filter(l -> l.getStatus() == RealEstatePropertyStatusEnum.SOLD)
                .mapToDouble(RealEstateProperty::getPrice)
                .average()
                .orElse(0);

        // Répartition par nombre de chambres (F2, F3, F4...)
        // APRÈS
        Map<String, Long> repartition = allLots.stream()
                .filter(l -> l.getStatus() == RealEstatePropertyStatusEnum.SOLD)
                .filter(l -> l.getPropertyType() != null)
                .collect(Collectors.groupingBy(
                        l -> l.getPropertyType().getTypeName(),
                        Collectors.counting()));
        /*
         * Map<Integer, Long> repartition = allLots.stream()
         * .filter(l -> l.getStatus() == RealEstatePropertyStatusEnum.SOLD)
         * .collect(Collectors.groupingBy(RealEstateProperty::getNumberOfRooms,
         * Collectors.counting()));
         */

        CommercialKpiDTO dto = new CommercialKpiDTO();
        dto.setTotalLots((int) totalLots);
        dto.setLotsVendus((int) lotsVendus);
        dto.setLotsReserves((int) lotsReserves);
        dto.setLotsDisponibles((int) lotsDisponibles);
        dto.setTauxCommercialisation(Math.round(tauxCommercialisation * 100.0) / 100.0);
        dto.setVitesseVente(vitesseVente);
        dto.setPrixMoyenVente(Math.round(prixMoyen * 100.0) / 100.0);
        dto.setRepartitionParNombreChambres(repartition);

        return dto;
    }

    private double calculerVitesseVente(List<RealEstateProperty> lots) {
        List<RealEstateProperty> lotsVendus = lots.stream()
                .filter(l -> l.getStatus() == RealEstatePropertyStatusEnum.SOLD && l.getSoldAt() != null)
                .collect(Collectors.toList());

        if (lotsVendus.isEmpty())
            return 0;

        LocalDateTime plusAncien = lotsVendus.stream()
                .map(RealEstateProperty::getSoldAt)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        long mois = java.time.temporal.ChronoUnit.MONTHS.between(plusAncien, LocalDateTime.now());
        if (mois == 0)
            mois = 1;

        return Math.round((lotsVendus.size() * 1.0 / mois) * 100.0) / 100.0;
    }
}