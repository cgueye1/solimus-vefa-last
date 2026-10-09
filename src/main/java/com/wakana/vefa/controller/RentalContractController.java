package com.wakana.vefa.controller;

import com.wakana.vefa.dto.RentalContractRequest;
import com.wakana.vefa.dto.response.RentalContractResponse;
import com.wakana.vefa.services.RentalContractService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/rental-contracts")
@RequiredArgsConstructor
public class RentalContractController {

    private final RentalContractService service;

    @PostMapping(consumes = "multipart/form-data")
    public RentalContractResponse create(
            @ModelAttribute RentalContractRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}/archive")
    public void archive(@PathVariable Long id) {
        service.archive(id);
    }

    @GetMapping("/active")
    public List<RentalContractResponse> activeContracts() {
        return service.getActiveContracts();
    }

    @GetMapping("/archived/property/{propertyId}")
    public List<RentalContractResponse> archivedByProperty(
            @PathVariable Long propertyId) {
        return service.getArchivedContracts(propertyId);
    }

    @GetMapping("/recipient/{recipientId}")
    public List<RentalContractResponse> byRecipient(@PathVariable Long recipientId) {
        return service.getContractsByRecipient(recipientId);
    }
}
