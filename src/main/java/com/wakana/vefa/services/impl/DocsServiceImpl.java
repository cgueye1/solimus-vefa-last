package com.wakana.vefa.services.impl;

import com.wakana.vefa.dto.DocsRequest;
import com.wakana.vefa.dto.SignatureRequest;
import com.wakana.vefa.dto.response.DocsResponse;
import com.wakana.vefa.model.Docs;
import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.model.SignatureAuditLog;
import com.wakana.vefa.model.SignatoryStatus;
import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.DocsRepository;
import com.wakana.vefa.repository.SignatureAuditLogRepository;
import com.wakana.vefa.repository.RealEstatePropertyRepository;
import com.wakana.vefa.repository.UserRepository;
import com.wakana.vefa.services.DocsService;
import com.wakana.vefa.util.FileTraferUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocsServiceImpl implements DocsService {

    private final DocsRepository docsRepository;
    private final RealEstatePropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final SignatureAuditLogRepository signatureAuditLogRepository;
    private final HttpServletRequest httpServletRequest;

    private DocsResponse mapToResponse(Docs docs) {
        DocsResponse response = new DocsResponse();
        BeanUtils.copyProperties(docs, response);
        response.setPropertyId(docs.getRealEstateProperty().getId());
        
        // Map signatories
        List<DocsResponse.SignatoryInfo> signatoryInfos = docs.getSignatories().stream()
                .map(s -> new DocsResponse.SignatoryInfo(
                        s.getSignatory().getId(),
                        s.getSignatory().getPrenom(),
                        s.getSignatory().getNom(),
                        s.isHasSigned(),
                        s.getSignedAt(),
                        s.getSignatureNotes()
                ))
                .collect(Collectors.toList());
        
        response.setSignatories(signatoryInfos);
        return response;
    }

    @Override
    @Transactional
    public DocsResponse create(DocsRequest request) {
        RealEstateProperty property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        Docs docs = new Docs();
        docs.setTitle(request.getTitre());
        docs.setRealEstateProperty(property);
        docs.setLastUpdated(LocalDateTime.now());

        try {
            if (request.getInitPdf() != null && !request.getInitPdf().isEmpty()) {
                String pdf =  FileTraferUtil.handleFileUpload(request.getInitPdf());
                docs.setInitPdf(pdf);
            }
        } catch (IOException e) {
            throw new RuntimeException("File upload error: " + e.getMessage());
        }

        // Add signatories if provided
        if (request.getSignatoryIds() != null && !request.getSignatoryIds().isEmpty()) {
            List<User> signatories = userRepository.findAllById(request.getSignatoryIds());
            signatories.forEach(docs::addSignatory);
        }

        Docs saved = docsRepository.save(docs);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public Optional<DocsResponse> update(Long id, DocsRequest request) {
        return docsRepository.findById(id).map(existing -> {
            existing.setTitle(request.getTitre());
            existing.setLastUpdated(LocalDateTime.now());

            if (request.getInitPdf() != null && !request.getInitPdf().isEmpty()) {
                try {
                    String pdf =  FileTraferUtil.handleFileUpload(request.getInitPdf());
                    existing.setInitPdf(pdf);
                } catch (IOException e) {
                    throw new RuntimeException("File upload error: " + e.getMessage());
                }
            }

            // Update signatories if provided
            if (request.getSignatoryIds() != null) {
                existing.clearSignatories();
                List<User> signatories = userRepository.findAllById(request.getSignatoryIds());
                signatories.forEach(existing::addSignatory);
            }

            return mapToResponse(docsRepository.save(existing));
        });
    }

    @Override
    public void delete(Long id) {
        docsRepository.deleteById(id);
    }

    @Override
    public List<DocsResponse> findAll() {
        return docsRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DocsResponse> findById(Long id) {
        return docsRepository.findByIdWithSignatories(id)
                .map(this::mapToResponse);
    }

    @Override
    public List<DocsResponse> findByPropertyId(Long propertyId) {
        return docsRepository.findByRealEstatePropertyId(propertyId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DocsResponse addSignedPdf(Long docId, MultipartFile signedPdf) {
        Docs docs = docsRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        try {
            String file =  FileTraferUtil.handleFileUpload(signedPdf);
            docs.setSignedPdf(file);
            docs.setLastUpdated(LocalDateTime.now());
        } catch (IOException e) {
            throw new RuntimeException("Error uploading signed file: " + e.getMessage());
        }

        return mapToResponse(docsRepository.save(docs));
    }

    @Override
    @Transactional
    public DocsResponse markAsSigned(SignatureRequest signatureRequest) {
        Docs docs = docsRepository.findById(signatureRequest.getDocumentId())
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        
        User user = userRepository.findById(signatureRequest.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Find the signatory status
        SignatoryStatus status = docs.getSignatories().stream()
                .filter(s -> s.getSignatory().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User is not a signatory for this document"));

        // Mark as signed
        status.setHasSigned(true);
        status.setSignedAt(LocalDateTime.now());
        status.setSignatureNotes(signatureRequest.getSignatureNotes());

        SignatureAuditLog auditLog = new SignatureAuditLog();
        auditLog.setDocument(docs);
        auditLog.setSignatory(user);
        auditLog.setProperty(docs.getRealEstateProperty());
        auditLog.setDocumentTitle(docs.getTitle());
        auditLog.setProjectName(docs.getRealEstateProperty().getName());
        auditLog.setSignerName(buildSignerName(user));
        auditLog.setSignerPhone(user.getTelephone());
        auditLog.setSignedAt(status.getSignedAt());
        auditLog.setClientIpAddress(resolveClientIpAddress(httpServletRequest));
        auditLog.setSignatureNotes(signatureRequest.getSignatureNotes());
        signatureAuditLogRepository.save(auditLog);

        // Upload signed PDF if provided
        if (signatureRequest.getSignedPdf() != null && !signatureRequest.getSignedPdf().isEmpty()) {
            try {
                String file =  FileTraferUtil.handleFileUpload(signatureRequest.getSignedPdf());
                docs.setSignedPdf(file);
            } catch (IOException e) {
                throw new RuntimeException("Error uploading signed file: " + e.getMessage());
            }
        }

        docs.setLastUpdated(LocalDateTime.now());
        
        return mapToResponse(docsRepository.save(docs));
    }

    private String buildSignerName(User user) {
        return String.join(" ",
                user.getPrenom() == null ? "" : user.getPrenom().trim(),
                user.getNom() == null ? "" : user.getNom().trim()).trim();
    }

    private String resolveClientIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        return realIp != null && !realIp.isBlank() ? realIp.trim() : request.getRemoteAddr();
    }

    @Override
    @Transactional
    public DocsResponse addSignatories(Long documentId, List<Long> userIds) {
        Docs docs = docsRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        List<User> newSignatories = userRepository.findAllById(userIds);
        
        // Filter out users that are already signatories
        List<Long> existingIds = docs.getSignatories().stream()
                .map(s -> s.getSignatory().getId())
                .collect(Collectors.toList());

        newSignatories.stream()
                .filter(user -> !existingIds.contains(user.getId()))
                .forEach(docs::addSignatory);

        docs.setLastUpdated(LocalDateTime.now());
        
        return mapToResponse(docsRepository.save(docs));
    }

    @Override
    @Transactional
    public DocsResponse removeSignatory(Long documentId, Long userId) {
        Docs docs = docsRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        docs.getSignatories().removeIf(s -> s.getSignatory().getId().equals(userId));
        docs.setLastUpdated(LocalDateTime.now());
        
        return mapToResponse(docsRepository.save(docs));
    }

    @Override
    public List<DocsResponse> findPendingSignaturesForUser(Long userId) {
        return docsRepository.findAll().stream()
                .filter(doc -> doc.getSignatories().stream()
                        .anyMatch(s -> s.getSignatory().getId().equals(userId) && !s.isHasSigned()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}