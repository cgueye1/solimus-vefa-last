package com.wakana.vefa.controller;

import com.wakana.vefa.dto.DashboardStats;
import com.wakana.vefa.dto.RealEstatePropertyRequest;
import com.wakana.vefa.dto.RealEstatePropertyResponse;
import com.wakana.vefa.dto.RealEstatePropertySearchRequest;
import com.wakana.vefa.dto.ReservationStats;
import com.wakana.vefa.enums.RealEstatePropertyStatusEnum;
import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.services.RealEstatePropertyService;
import com.wakana.vefa.util.FileTraferUtil;
import com.wakana.vefa.util.ProfilEnumUtils;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;

@RestController
@RequestMapping("/api/realestate")
@RequiredArgsConstructor
public class RealEstatePropertyController {

    private final RealEstatePropertyService realEstatePropertyService;

    @PostMapping("/save")
    public ResponseEntity<?> save(@ModelAttribute RealEstatePropertyRequest request) {
        try {

            String planFileName = FileTraferUtil.handleFileUpload(request.getPlan());

            String legalStatusFileName = FileTraferUtil.handleFileUpload(request.getLegalStatus());

            List<String> pictureUrls = FileTraferUtil.uploadPictures(request.getPictures());

            RealEstateProperty savedProperty = realEstatePropertyService.save(
                    request, planFileName, legalStatusFileName, pictureUrls);

            return ResponseEntity.ok(Collections.singletonMap("realEstateProperty", "Saved"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Échec d'enregistrement : " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @ModelAttribute RealEstatePropertyRequest request) {
        try {

            String planFileName = FileTraferUtil.handleFileUpload(request.getPlan());
            String legalStatusFileName = FileTraferUtil.handleFileUpload(request.getLegalStatus());
            List<String> pictureUrls = FileTraferUtil.uploadPictures(request.getPictures());
            RealEstateProperty updatedProperty = realEstatePropertyService.update(
                    id, request, planFileName, legalStatusFileName, pictureUrls);
            return ResponseEntity.ok(Collections.singletonMap("realEstateProperty", updatedProperty));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Échec de la mise à jour : " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            realEstatePropertyService.delete(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Échec de la suppression : " + e.getMessage());
        }
    }

    @PostMapping("/search-by-promoter")
    public ResponseEntity<Page<?>> searchByPromoter(
            @RequestBody RealEstatePropertySearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<?> realEstatePropertyPage = realEstatePropertyService
                .searchByPromoterAndParentIsNull(request.getPromoterId(),
                        ProfilEnumUtils.toProfilEnum(request.getPlanName()), pageable);

        long totalElements = realEstatePropertyPage.getTotalElements();
        int totalPages = realEstatePropertyPage.getTotalPages();

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("X-Total-Elements", String.valueOf(totalElements));
        responseHeaders.add("X-Total-Pages", String.valueOf(totalPages));

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(realEstatePropertyPage);
    }

    @PostMapping("/search-by-bank")
    public ResponseEntity<Page<?>> searchByBank(
            @RequestBody RealEstatePropertySearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<?> realEstatePropertyPage = realEstatePropertyService
                .searchByBankAndParentIsNull(request.getPromoterId(), pageable);

        long totalElements = realEstatePropertyPage.getTotalElements();
        int totalPages = realEstatePropertyPage.getTotalPages();

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("X-Total-Elements", String.valueOf(totalElements));
        responseHeaders.add("X-Total-Pages", String.valueOf(totalPages));

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(realEstatePropertyPage);
    }

    @PostMapping("/search-by-agency")
    public ResponseEntity<Page<?>> searchByAgency(
            @RequestBody RealEstatePropertySearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<?> realEstatePropertyPage = realEstatePropertyService
                .searchByAgencyAndParentIsNull(request.getPromoterId(),
                        ProfilEnumUtils.toProfilEnum(request.getPlanName()), pageable);

        long totalElements = realEstatePropertyPage.getTotalElements();
        int totalPages = realEstatePropertyPage.getTotalPages();

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("X-Total-Elements", String.valueOf(totalElements));
        responseHeaders.add("X-Total-Pages", String.valueOf(totalPages));

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(realEstatePropertyPage);
    }

    @PostMapping("/search-by-parent")
    public ResponseEntity<Page<RealEstateProperty>> searchByParentProperty(
            @RequestBody RealEstatePropertySearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RealEstateProperty> realEstatePropertyPage = realEstatePropertyService
                .findByParentPropertyId(request.getParentPropertyId(), pageable);

        long totalElements = realEstatePropertyPage.getTotalElements();
        int totalPages = realEstatePropertyPage.getTotalPages();

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("X-Total-Elements", String.valueOf(totalElements));
        responseHeaders.add("X-Total-Pages", String.valueOf(totalPages));

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(realEstatePropertyPage);
    }

    @PostMapping("/available-search-by-parent")
    public ResponseEntity<Page<RealEstateProperty>> searchAvailableByParentProperty(
            @RequestBody RealEstatePropertySearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RealEstateProperty> realEstatePropertyPage = realEstatePropertyService
                .findAvailableByParentPropertyId(request.getParentPropertyId(), pageable);

        long totalElements = realEstatePropertyPage.getTotalElements();
        int totalPages = realEstatePropertyPage.getTotalPages();

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("X-Total-Elements", String.valueOf(totalElements));
        responseHeaders.add("X-Total-Pages", String.valueOf(totalPages));

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(realEstatePropertyPage);
    }

    @PostMapping("/search-portail")
    public Page<RealEstateProperty> searchProperties(@RequestBody RealEstatePropertySearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");

        Pageable pageable = PageRequest.of(page, size, sort);
        return realEstatePropertyService.searchProperties(request, pageable);
    }

    @PostMapping("/search-lot-by-parent")
    public Page<RealEstateProperty> searchLotsByParent(@RequestBody RealEstatePropertySearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");

        Pageable pageable = PageRequest.of(page, size, sort);
        return realEstatePropertyService.searchLotPropertiesByParent(request, pageable);
    }

    @GetMapping("/change-status/{id}/{status}")
    public ResponseEntity<?> changePropertyStatus(@PathVariable Long id, @PathVariable String status) {
        try {
            RealEstatePropertyStatusEnum statusEnum = RealEstatePropertyStatusEnum.valueOf(status.toUpperCase());
            RealEstateProperty updatedProperty = realEstatePropertyService.changeStatus(id, statusEnum);

            return ResponseEntity.ok(Collections.singletonMap("realEstateProperty", updatedProperty));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status value");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating status: " + e.getMessage());
        }
    }

    @GetMapping("/change-recipient/{propertyId}")
    public ResponseEntity<?> changeRecipient(@PathVariable Long propertyId,
            @RequestParam(required = false) Long userId) {
        try {
            RealEstateProperty updatedProperty = realEstatePropertyService.changeRecipient(propertyId, userId);

            return ResponseEntity.ok(Collections.singletonMap("realEstateProperty", updatedProperty));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating recipient: " + e.getMessage());
        }
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<?> details(@PathVariable Long id) {
        try {

            return ResponseEntity
                    .ok(Collections.singletonMap("realEstateProperty", realEstatePropertyService.getProperty(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status value");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating status: " + e.getMessage());
        }
    }

    @GetMapping("/recipient/{recipientId}")
    public Page<RealEstateProperty> getPropertiesByRecipient(
            @PathVariable Long recipientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");

        Pageable pageable = PageRequest.of(page, size, sort);
        return realEstatePropertyService.getPropertiesByRecipientId(recipientId, pageable);
    }

    @GetMapping("/recipient/{recipientId}/all")
    public Page<RealEstateProperty> getPropertiesByRecipientAll(
            @PathVariable Long recipientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");

        Pageable pageable = PageRequest.of(page, size, sort);
        return realEstatePropertyService.getPropertiesByRecipientIdAll(recipientId, pageable);
    }

    @GetMapping("/notary/{notaryId}")
    public Page<RealEstateProperty> getReservedPropertiesByNotaryId(
            @PathVariable Long notaryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,

            RealEstatePropertyStatusEnum status,
            @RequestParam(required = false) String search

    ) {
        Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");

        Pageable pageable = PageRequest.of(page, size, sort);
        return realEstatePropertyService.getReservedPropertiesByNotaryId(notaryId, search, status, pageable);
    }

    @GetMapping("/sold/{id}")
    public ResponseEntity<?> sold(@PathVariable Long id) {
        try {

            return ResponseEntity
                    .ok(realEstatePropertyService.sold(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status value");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating status: " + e.getMessage());
        }
    }

    @GetMapping("/count-no-parent")
    public ResponseEntity<?> countPropertiesWithoutParent() {

        return ResponseEntity.ok(realEstatePropertyService.countPropertiesWithoutParent());
    }

    /// dashBord
    @GetMapping("/dash/promoter/{id}/{planName}")
    public DashboardStats getDashboardStats(@PathVariable Long id, @PathVariable String planName) {
        return realEstatePropertyService.getDashboardStats(id, planName);
    }

    /// copropriete
    @GetMapping("/{id}/{ownerId}/{area}")
    public ResponseEntity<?> addOwner(@PathVariable Long id, @PathVariable Long ownerId, @PathVariable double area) {
        return ResponseEntity
                .ok(realEstatePropertyService.addOwner(id, ownerId, area));
    }

    @PostMapping("/search-by-syndic")
    public ResponseEntity<Page<?>> searchBySyndic(
            @RequestBody RealEstatePropertySearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<?> realEstatePropertyPage = realEstatePropertyService
                .searchByPromoterAndSyndic(
                        request.getPromoterId(),
                        ProfilEnumUtils.toProfilEnum(request.getPlanName()),
                        pageable);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("X-Total-Elements",
                String.valueOf(realEstatePropertyPage.getTotalElements()));
        responseHeaders.add("X-Total-Pages",
                String.valueOf(realEstatePropertyPage.getTotalPages()));

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(realEstatePropertyPage);
    }

    @GetMapping("/{id}/proreties-by-owner")
    public ResponseEntity<Set<RealEstateProperty>> getOwners(@PathVariable Long id) {
        Set<RealEstateProperty> propreties = realEstatePropertyService.findPropertiesByOwnerId(id);
        return propreties.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(propreties);
    }

    @GetMapping("/budget/{id}/{budget}")
    public ResponseEntity<?> addOwner(@PathVariable Long id, @PathVariable double budget) {
        return ResponseEntity
                .ok(realEstatePropertyService.addBudget(id, budget));
    }

    @GetMapping("/allocate-budget/{id}")
    public ResponseEntity<?> allocateBudgetAndGenerateCalls(@PathVariable Long id) {
        realEstatePropertyService.allocateBudgetAndGenerateCalls(id);
        return ResponseEntity
                .ok("Geèeré");
    }

    //// location

    @PostMapping("/rental/search-by-promoter")
    public ResponseEntity<Page<?>> searchByPromoterRental(
            @RequestBody RealEstatePropertySearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<?> realEstatePropertyPage = realEstatePropertyService
                .searchByPromoterAndRental(request.getPromoterId(), ProfilEnumUtils.toProfilEnum(request.getPlanName()),
                        pageable);

        long totalElements = realEstatePropertyPage.getTotalElements();
        int totalPages = realEstatePropertyPage.getTotalPages();

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("X-Total-Elements", String.valueOf(totalElements));
        responseHeaders.add("X-Total-Pages", String.valueOf(totalPages));

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(realEstatePropertyPage);
    }

    @PostMapping("/rental/search-portail")
    public Page<RealEstateProperty> searchPropertiesRental(@RequestBody RealEstatePropertySearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");

        Pageable pageable = PageRequest.of(page, size, sort);
        return realEstatePropertyService.searchPropertiesRental(request, pageable);
    }

    ///// dashBoad

    @GetMapping("/sales/dashboard")
    public ResponseEntity<List<ReservationStats>> getSalesStats(
            @RequestParam int year,
            @RequestParam Long promoterId,
            @RequestParam(required = false) Long parentPropertyId

    ) {

        if (parentPropertyId != null) {

            return ResponseEntity
                    .ok(realEstatePropertyService.getSalesByMonthByPropertyId(year, parentPropertyId, promoterId));
        }

        return ResponseEntity.ok(realEstatePropertyService.getSalesByMonth(year, promoterId));
    }

    @GetMapping("/lots/dashboard")
    public DashboardStats getPropertyLotsDashboard(
            @RequestParam Long parentPropertyId,
            @RequestParam String planName) {
        return realEstatePropertyService
                .getLotsDashboardStats(parentPropertyId, planName);
    }

    /// cancel
    // reservation
    @GetMapping("/cancel/res/{id}")
    public ResponseEntity<?> cancelRes(@PathVariable Long id) {
        realEstatePropertyService.cancelRes(id);
        return ResponseEntity
                .ok(Map.of("message", "cancelled"));
    }

    @PostMapping("/{id}/subscription")
    public ResponseEntity<RealEstateProperty> addSubscription(
            @PathVariable Long id,
            @RequestParam String profil) {

        RealEstateProperty updatedProperty = realEstatePropertyService.addSub(id, profil);
        return ResponseEntity.ok(updatedProperty);
    }

    /**
     * Supprimer un owner d'une propriété
     */
    @DeleteMapping("/{propertyId}/owners/{ownerId}")
    public ResponseEntity<RealEstatePropertyResponse> removeOwner(
            @PathVariable Long propertyId,
            @PathVariable Long ownerId) {
        RealEstatePropertyResponse response = realEstatePropertyService.removeOwner(propertyId, ownerId);

        return ResponseEntity.ok(response);
    }
}
