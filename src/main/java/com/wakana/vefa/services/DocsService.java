package com.wakana.vefa.services;

import com.wakana.vefa.dto.DocsRequest;
import com.wakana.vefa.dto.SignatureRequest;
import com.wakana.vefa.dto.response.DocsResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

public interface DocsService {

    DocsResponse create(DocsRequest request);

    Optional<DocsResponse> update(Long id, DocsRequest request);

    void delete(Long id);

    List<DocsResponse> findAll();

    Optional<DocsResponse> findById(Long id);

    List<DocsResponse> findByPropertyId(Long propertyId);

    DocsResponse addSignedPdf(Long docId, MultipartFile signedPdf);

    // New methods for signature management
    DocsResponse markAsSigned(SignatureRequest signatureRequest);

    DocsResponse addSignatories(Long documentId, List<Long> userIds);

    DocsResponse removeSignatory(Long documentId, Long userId);

    List<DocsResponse> findPendingSignaturesForUser(Long userId);

}