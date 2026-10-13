package com.wakana.vefa.controller;

import com.wakana.vefa.model.PaymentCall;
import com.wakana.vefa.services.PaymentCallService;
import com.wakana.vefa.util.FileTraferUtil;
import io.jsonwebtoken.io.IOException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.wakana.vefa.dto.PaymentCallRequest;
import com.wakana.vefa.dto.response.CallForChargeKpiResponse;
import com.wakana.vefa.dto.response.PaymentCallKpiDto;
import com.wakana.vefa.services.PaymentCallPdfService;
import com.wakana.vefa.enums.PaymentCallStatusEnum;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.wakana.vefa.model.User;

import java.util.Map;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/payment-calls")
@RequiredArgsConstructor
public class PaymentCallController {

    private final PaymentCallService paymentCallService;
    private final PaymentCallPdfService paymentCallPdfService;

    @GetMapping("/property/{propertyId}")
    public List<PaymentCall> getPaymentCallsByProperty(@PathVariable Long propertyId) {
        return paymentCallService.getPaymentCallsForProperty(propertyId);
    }

    @GetMapping("/me")
    public ResponseEntity<List<PaymentCall>> getPaymentCallsForCurrentUser(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User currentUser)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(paymentCallService.getPaymentCallsForRecipient(currentUser.getId()));
    }

        @GetMapping(value = { "/{id}/receipt", "/{id}/pdf" }, produces = MediaType.APPLICATION_PDF_VALUE)
        public ResponseEntity<byte[]> downloadPaymentCallReceipt(@PathVariable Long id) {
        PaymentCall paymentCall = paymentCallService.getPaymentCall(id)
            .orElseThrow(() -> new EntityNotFoundException("Appel de fonds introuvable"));
        if (paymentCall.getStatus() != PaymentCallStatusEnum.PAID) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        byte[] pdf = paymentCallPdfService.generateReceiptPdf(paymentCall);
        String filename = "quittance-" + id + ".pdf";

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
        }

    @PostMapping
    @PreAuthorize("hasAuthority('PROMOTEUR')")
    public PaymentCall createPaymentCall(@RequestBody PaymentCallRequest paymentCallRequest) {
        return paymentCallService.createPaymentCall(paymentCallRequest);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROMOTEUR')")
    public ResponseEntity<PaymentCall> updatePaymentCall(@PathVariable Long id,
            @RequestBody PaymentCallRequest paymentCallRequest) {
        Optional<PaymentCall> updatedPaymentCall = paymentCallService.updatePaymentCall(id, paymentCallRequest);
        return updatedPaymentCall.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Endpoint to simulate payment status and return overdue payments and total
    // amount
    @GetMapping("/simulate-payments/{id}")
    public ResponseEntity<Map<String, Object>> simulatePayments(@PathVariable Long id) {
        try {
            Map<String, Object> result = paymentCallService.simulatePayments(id);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROMOTEUR')")
    public void deletePaymentCall(@PathVariable Long id) {
        paymentCallService.deletePaymentCall(id);
    }

    @PostMapping("/{id}/file")
    public ResponseEntity<?> save(@ModelAttribute PaymentCallRequest request, @PathVariable Long id)
            throws java.io.IOException {
        try {

            String file = FileTraferUtil.handleFileUpload(request.getFile());

            request.setFileName(file);

            return ResponseEntity.ok(paymentCallService.addFile(id, request));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Échec d'enregistrement : " + e.getMessage());
        }
    }


    @GetMapping("/kpi")
    public ResponseEntity<CallForChargeKpiResponse> getKpis(
            @RequestParam(required = false) Long promoterId,
            @RequestParam(required = false) Long propertyId,
            @RequestParam(required = true) String planName

    ) {

        if (promoterId != null) {
            return ResponseEntity.ok(paymentCallService.getKpiByPromoter(promoterId, planName));
        } else if (propertyId != null) {
            return ResponseEntity.ok(paymentCallService.getKpiByParentProperty(propertyId));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
    
    
    
     /**
     * Exemple :
     * PUT /api/payment-calls/12/status?status=PAID
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<PaymentCall> updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(
                paymentCallService.updateStatus(id, status)
        );
    }
    
    
    
    @GetMapping("/{propertyId}/kpi")
    public PaymentCallKpiDto getKpis(@PathVariable Long propertyId) {
        return   paymentCallService.getPaymentCallKpis(propertyId);
    }
}
