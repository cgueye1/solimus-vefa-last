package com.wakana.vefa.services;


import com.wakana.vefa.dto.RentalContractRequest;
import com.wakana.vefa.dto.response.RentalContractResponse;

import java.util.List;

public interface RentalContractService {

    RentalContractResponse create(RentalContractRequest request);

    void archive(Long contractId);

    List<RentalContractResponse> getActiveContracts();

    List<RentalContractResponse> getArchivedContracts(Long propertyId);

    List<RentalContractResponse> getContractsByRecipient(Long recipientId);
}
