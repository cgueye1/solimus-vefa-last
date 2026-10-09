package com.wakana.vefa.services.impl;

import com.wakana.vefa.dto.MeetFileRequest;
import com.wakana.vefa.dto.WorkRequest;
import com.wakana.vefa.dto.response.RealEstatePropertyKpiResponse;
import com.wakana.vefa.dto.response.WorkKpiResponse;
import com.wakana.vefa.enums.PaymentCallStatusEnum;
import com.wakana.vefa.enums.WorkStatusEnum;
import com.wakana.vefa.model.*;
import com.wakana.vefa.repository.*;
import com.wakana.vefa.services.WorkService;
import com.wakana.vefa.util.FileTraferUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkServiceImpl implements WorkService {

    private final WorkRepository workRepository;

    private final WorkFilesRepository workFilesRepository;

    private final RealEstatePropertyRepository realEstatePropertyRepository;
    private final OwnerPropertyAreaRepository ownerPropertyAreaRepository;
    private final CallForChargeRepository callForChargeRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override
    public Work createWork(Long propertyId, WorkRequest request) {
        RealEstateProperty property = realEstatePropertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Propriété non trouvée"));

        Work work = new Work();
        // Copier uniquement les champs compatibles
        work.setTitre(request.getTitre());
        work.setDescription(request.getDescription());
        work.setPrice(request.getPrice());

        // Parser manuellement les dates
        if (request.getStartDate() != null && !request.getStartDate().isEmpty()) {
            work.setStartDate(LocalDate.parse(request.getStartDate(), FORMATTER));
        }
        if (request.getDeliveryDate() != null && !request.getDeliveryDate().isEmpty()) {
            work.setDeliveryDate(LocalDate.parse(request.getDeliveryDate(), FORMATTER));
        }

        work.setRealEstateProperty(property);

        work = workRepository.save(work);

        // Générer appels de charge si demandé
        if (request.isGenerateCallForCharge()) {
            generateCallsForWork(work);
        }

        return work;
    }

    private void generateCallsForWork(Work work) {
        List<OwnerPropertyArea> ownerAreas = ownerPropertyAreaRepository
                .findByRealEstatePropertyId(work.getRealEstateProperty().getId());

        double totalArea = ownerAreas.stream()
                .mapToDouble(OwnerPropertyArea::getAreaInSquareMeters)
                .sum();

        for (OwnerPropertyArea ownerArea : ownerAreas) {
            User owner = ownerArea.getOwner();
            double ownerAreaSize = ownerArea.getAreaInSquareMeters();
            double ownershipPercentage = ownerAreaSize / totalArea;
            double ownerBudgetShare = ownershipPercentage * work.getPrice();

            CallForCharge call = new CallForCharge();
            call.setLabel(work.getTitre());
            call.setFee(ownerBudgetShare);

            // ⚡ Même format que LocalDate.now().plusMonths(1).toString() → yyyy-MM-dd
            if (work.getStartDate() != null) {
                call.setExpectedDate(work.getStartDate().toString());
            }

            call.setStatus(PaymentCallStatusEnum.PENDING);
            call.setRealEstateProperty(work.getRealEstateProperty());
            call.setOwner(owner);
            call.setWork(work);
            call.setCreatedAt(System.currentTimeMillis());

            callForChargeRepository.save(call);
        }
    }

    @Override
    public Work updateWork(Long id, WorkRequest request) {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Travail non trouvé"));
        BeanUtils.copyProperties(request, work, "id", "realEstateProperty", "calls");
        return workRepository.save(work);
    }

    @Override
    public void deleteWork(Long id) {
        workRepository.deleteById(id);
    }

    @Override
    public Page<Work> listWorksByProperty(Long propertyId, String month, Pageable pageable) {
        if (month != null && !month.isEmpty()) {
            return workRepository.findByRealEstatePropertyIdAndMonth(propertyId, month, pageable);
        }
        return workRepository.findByRealEstatePropertyId(propertyId, pageable);
    }

    @Override
    public Work changeStatus(Long id, String status) {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Travail non trouvé"));
        work.setStatus(WorkStatusEnum.valueOf(status));
        return workRepository.save(work);
    }

    @Override
    public Work addPictures(Long workId, List<MultipartFile> files) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new IllegalArgumentException("Travail non trouvé"));

        try {
            List<String> urls = FileTraferUtil.uploadPictures(files); // ton utilitaire
            work.getPictures().addAll(urls);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l’upload des images", e);
        }

        return workRepository.save(work);
    }

    @Override
    public Work removePicture(Long workId, String pictureUrl) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new IllegalArgumentException("Travail non trouvé"));

        work.getPictures().remove(pictureUrl);

        return workRepository.save(work);
    }

    public WorkKpiResponse getKpiByWork(Long workId) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new RuntimeException("Work not found"));

        List<CallForCharge> charges = callForChargeRepository.findByWork(work);

        int totalInvoices = charges.size();
        double totalCharges = charges.stream().mapToDouble(CallForCharge::getFee).sum();
        double totalPaid = charges.stream()
                .filter(c -> c.getStatus() == PaymentCallStatusEnum.PAID)
                .mapToDouble(CallForCharge::getFee)
                .sum();
        double totalPending = totalCharges - totalPaid;

        return new WorkKpiResponse(workId, totalInvoices, totalCharges, totalPaid, totalPending);
    }

    @Override
    public RealEstatePropertyKpiResponse getKpi(Long propertyId, String month) {
        RealEstateProperty property = realEstatePropertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        // 🔹 Récupérer les travaux
        List<Work> works = workRepository.findByRealEstateProperty(property);

        // 🔹 Récupérer toutes les charges
        List<CallForCharge> charges = callForChargeRepository.findByRealEstateProperty(property);

        // Si un mois est fourni -> filtrer
        if (month != null && !month.isEmpty()) {
            works = works.stream()
                    .filter(w -> w.getStartDate() != null &&
                            w.getStartDate().toString().startsWith(month)) // yyyy-MM
                    .toList();

            charges = charges.stream()
                    .filter(c -> c.getExpectedDate() != null &&
                            c.getExpectedDate().startsWith(month)) // attendu au format yyyy-MM-dd
                    .toList();
        }

        int totalWorks = works.size();

        double totalCharges = charges.stream().mapToDouble(CallForCharge::getFee).sum();
        double totalCollected = charges.stream()
                .filter(c -> c.getStatus() == PaymentCallStatusEnum.PAID)
                .mapToDouble(CallForCharge::getFee)
                .sum();
        double totalPending = totalCharges - totalCollected;

        return new RealEstatePropertyKpiResponse(
                propertyId, totalWorks, totalCharges, totalCollected, totalPending);
    }

    public List<WorkFiles> getFilesByWorkId(Long meetId) {
        return workFilesRepository.findByWorkId(meetId);
    }

    @Override
    public WorkFiles addFile(MeetFileRequest request) {
        Work work = workRepository.findById(request.getMeetId())
                .orElseThrow(() -> new IllegalArgumentException("Meet not found with id: " + request.getMeetId()));
        WorkFiles workFile = new WorkFiles();
        workFile.setFile(request.getFileName());
        workFile.setLabel(request.getLabel());
        workFile.setWork(work);
        return workFilesRepository.save(workFile);
    }

    @Override
    public Optional<Work> getworkDetail(Long workId) {
        return workRepository.findById(workId);

    }
}
