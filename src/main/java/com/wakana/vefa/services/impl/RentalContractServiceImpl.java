package com.wakana.vefa.services.impl;

import com.wakana.vefa.dto.RentalContractRequest;
import com.wakana.vefa.dto.response.RentalContractResponse;
import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.model.RentalContract;
import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.RealEstatePropertyRepository;
import com.wakana.vefa.repository.RentalContractRepository;
import com.wakana.vefa.repository.UserRepository;
import com.wakana.vefa.services.RentalContractService;
import com.wakana.vefa.util.DateUtil;
import com.wakana.vefa.util.FileTraferUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalContractServiceImpl implements RentalContractService {

    private final RentalContractRepository repository;
    private final RealEstatePropertyRepository propertyRepository;
    private final UserRepository userRepository;

    @Override
    public RentalContractResponse create(RentalContractRequest request) {

        RealEstateProperty property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new RuntimeException("Property not found"));

        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        RentalContract contract = new RentalContract();
        contract.setProperty(property);
        contract.setRecipient(recipient);

        // ✅ conversion String -> LocalDate
        contract.setStartDate(DateUtil.parse(request.getStartDate()));
        contract.setEndDate(DateUtil.parse(request.getEndDate()));

        contract.setCreatedAt(LocalDateTime.now());
        contract.setActive(true);
        contract.setArchived(false);

        try {
            if (request.getContractFile() != null && !request.getContractFile().isEmpty()) {
                String fileName = FileTraferUtil.handleFileUpload(request.getContractFile());
                contract.setContractFile(fileName);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload rental contract file", e);
        }

        repository.save(contract);

        return mapToResponse(contract);
    }

    @Override
    public void archive(Long contractId) {
        RentalContract contract = repository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        contract.setArchived(true);
        contract.setActive(false);
        contract.setArchivedAt(LocalDateTime.now());

        repository.save(contract);
    }

    @Override
    public List<RentalContractResponse> getActiveContracts() {
        return repository.findByArchivedFalse()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

 @Override
public List<RentalContractResponse> getArchivedContracts(Long propertyId) {
    return repository.findByPropertyIdAndArchivedTrue(propertyId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
}
    @Override
    public List<RentalContractResponse> getContractsByRecipient(Long recipientId) {
        return repository.findByRecipientIdAndArchivedFalse(recipientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private RentalContractResponse mapToResponse(RentalContract contract) {
        RentalContractResponse dto = new RentalContractResponse();
        dto.setId(contract.getId());
        dto.setPropertyId(contract.getProperty().getId());
        dto.setPropertyName(contract.getProperty().getName());
        dto.setRecipientId(contract.getRecipient().getId());
        dto.setRecipientName(contract.getRecipient().getNom());
        dto.setStartDate(contract.getStartDate());
        dto.setEndDate(contract.getEndDate());
        dto.setActive(contract.isActive());
        dto.setArchived(contract.isArchived());
        dto.setContractFile(contract.getContractFile());
        dto.setCreatedAt(contract.getCreatedAt());
        return dto;
    }
}
