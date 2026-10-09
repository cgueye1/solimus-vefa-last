package com.wakana.vefa.services.impl.v2;

import com.wakana.vefa.dto.v2.EncaissementDetailDTO;
import com.wakana.vefa.dto.v2.FinancierKpiDTO;
import com.wakana.vefa.enums.PaymentCallStatusEnum;
import com.wakana.vefa.enums.RealEstatePropertyStatusEnum;
import com.wakana.vefa.model.PaymentCall;
import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.repository.LotDashboardRepository;
import com.wakana.vefa.repository.PaymentCallRepository;
import com.wakana.vefa.services.V2.FinancierDashboardService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancierDashboardServiceImpl implements FinancierDashboardService {

    private final PaymentCallRepository paymentCallRepo;
    private final LotDashboardRepository lotRepo;

    @Override
    public FinancierKpiDTO getFinancierKpi(Long projectId) {
        List<RealEstateProperty> allLots = lotRepo.findLotsByProjectId(projectId);
        List<PaymentCall> allPayments = paymentCallRepo.findAllByProjectId(projectId);

        // CA signé = somme prix lots SOLD
        double caSigné = allLots.stream()
                .filter(l -> l.getStatus() == RealEstatePropertyStatusEnum.SOLD)
                .mapToDouble(RealEstateProperty::getPrice)
                .sum();

        // Encaissements
        double totalRecu = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentCallStatusEnum.PAID)
                .mapToDouble(PaymentCall::getAmount)
                .sum();

        double totalPrevu = allPayments.stream()
                .mapToDouble(PaymentCall::getAmount)
                .sum();

        double resteAEncaisser = totalPrevu - totalRecu;

        double tauxRecouvrement = totalPrevu > 0
                ? Math.round((totalRecu / totalPrevu * 100) * 100.0) / 100.0
                : 0;

        // Analyse clients via lots
        // Client à jour = aucun PaymentCall OVERDUE sur ses lots
        // Client en retard = au moins un OVERDUE
        // Client à risque = plusieurs OVERDUE

        List<PaymentCall> overduePayments = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentCallStatusEnum.OVERDUE)
                .collect(Collectors.toList());

        // Grouper par recipient (client du lot)
        long clientsEnRetard = overduePayments.stream()
                .map(p -> p.getRealEstateProperty().getRecipient())
                .filter(u -> u != null)
                .map(u -> u.getId())
                .distinct()
                .count();

        long clientsArisque = overduePayments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getRealEstateProperty().getRecipient() != null
                                ? p.getRealEstateProperty().getRecipient().getId()
                                : -1L,
                        Collectors.counting()))
                .entrySet().stream()
                .filter(e -> e.getKey() != -1L && e.getValue() >= 2)
                .count();

        long totalClients = allLots.stream()
                .map(RealEstateProperty::getRecipient)
                .filter(u -> u != null)
                .map(u -> u.getId())
                .distinct()
                .count();

        long clientsAJour = totalClients - clientsEnRetard;

        FinancierKpiDTO dto = new FinancierKpiDTO();
        dto.setChiffreAffairesSigne(Math.round(caSigné * 100.0) / 100.0);
        dto.setTotalEncaissementsRecus(Math.round(totalRecu * 100.0) / 100.0);
        dto.setTotalEncaissementsPrevus(Math.round(totalPrevu * 100.0) / 100.0);
        dto.setResteAEncaisser(Math.round(resteAEncaisser * 100.0) / 100.0);
        dto.setTauxRecouvrement(tauxRecouvrement);
        dto.setClientsAJour(Math.max(clientsAJour, 0));
        dto.setClientsEnRetard(clientsEnRetard);
        dto.setClientsArisque(clientsArisque);

        return dto;
    }

    @Override
    public List<EncaissementDetailDTO> getEcheancierDetail(Long projectId) {
        List<PaymentCall> payments = paymentCallRepo.findAllByProjectId(projectId);

        return payments.stream().map(p -> {
            EncaissementDetailDTO dto = new EncaissementDetailDTO();
            dto.setLotId(p.getRealEstateProperty().getId());
            dto.setLotName(p.getRealEstateProperty().getName());
            dto.setLotNumber(p.getRealEstateProperty().getNumber());
            dto.setLabel(p.getLabel());
            dto.setRef(p.getRef());
            dto.setMontantPrevu(p.getAmount());
            dto.setMontantRecu(p.getStatus() == PaymentCallStatusEnum.PAID ? p.getAmount() : 0);
            dto.setDateAttendue(p.getExpectedDate());
            dto.setStatus(p.getStatus());

            if (p.getRealEstateProperty().getRecipient() != null) {
                dto.setClientNom(p.getRealEstateProperty().getRecipient().getNom());
                dto.setClientPrenom(p.getRealEstateProperty().getRecipient().getPrenom());
            }

            return dto;
        }).collect(Collectors.toList());
    }
}