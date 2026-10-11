package com.wakana.vefa.controller;

import com.wakana.vefa.dto.response.SignatureAuditLogResponse;
import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.SignatureAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/signature-audit")
@RequiredArgsConstructor
public class SignatureAuditController {

    private final SignatureAuditLogRepository signatureAuditLogRepository;

    @GetMapping
    public ResponseEntity<Page<SignatureAuditLogResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "signedAt"));

        Page<SignatureAuditLogResponse> response;
        if (currentUser.getProfil() == ProfilEnum.ADMIN) {
            response = signatureAuditLogRepository.search(keyword, pageable)
                    .map(SignatureAuditLogResponse::new);
        } else if (currentUser.getProfil() == ProfilEnum.PROMOTEUR) {
            response = signatureAuditLogRepository.searchForPromoter(currentUser.getId(), keyword, pageable)
                    .map(SignatureAuditLogResponse::new);
        } else if (currentUser.getProfil() == ProfilEnum.RESERVATAIRE) {
            response = signatureAuditLogRepository.searchForSignatory(currentUser.getId(), keyword, pageable)
                .map(SignatureAuditLogResponse::new);
        } else {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(response);
    }
}