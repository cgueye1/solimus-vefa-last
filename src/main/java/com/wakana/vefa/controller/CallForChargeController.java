package com.wakana.vefa.controller;

import com.wakana.vefa.dto.PaymentCallRequest;
import com.wakana.vefa.dto.response.CallForChargeKpiResponse;
import com.wakana.vefa.enums.PaymentCallStatusEnum;
import com.wakana.vefa.model.CallForCharge;
import com.wakana.vefa.services.CallForChargeService;
import com.wakana.vefa.util.FileTraferUtil;

import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/call-for-charges")
@RequiredArgsConstructor
public class CallForChargeController {

    private final CallForChargeService callForChargeService;

    @GetMapping("/pending")
    public Page<CallForCharge> getPendingCallsByOwner(@RequestParam Long ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);

        return callForChargeService.getPendingCallsByOwner(ownerId, pageable);
    }

    @GetMapping("/paid")
    public Page<CallForCharge> getPaidCallsByOwner(@RequestParam Long ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return callForChargeService.getPaidCallsByOwner(ownerId, pageable);
    }

    @GetMapping
    public Page<CallForCharge> getCallsByOwnerAndProperty(@RequestParam Long ownerId, @RequestParam Long propertyId,
            Pageable pageable) {
        return callForChargeService.getCallsByOwnerAndProperty(ownerId, propertyId, pageable);
    }

    @GetMapping("/earliest-pending")
    public ResponseEntity<List<CallForCharge>> getEarliestPendingCallsByOwner(@RequestParam Long ownerId) {
        List<CallForCharge> calls = callForChargeService.getEarliestPendingCallsByOwner(ownerId);
        return ResponseEntity.ok(calls);
    }

    @GetMapping("/pending-fees")
    public double getPendingFees(@RequestParam Long ownerId) {
        return callForChargeService.getPendingFeesForOwner(ownerId);
    }

    @GetMapping("/proprety/pending")
    public Page<CallForCharge> getPendingCallsByOwnerAndProperty(
            @RequestParam Long ownerId,
            @RequestParam Long propertyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return callForChargeService.getPendingCallsByOwnerAndProprety(ownerId, propertyId, pageable);
    }

    @GetMapping("/proprety/paid")
    public Page<CallForCharge> getPaidCallsByOwnerAndProperty(
            @RequestParam Long ownerId,
            @RequestParam Long propertyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return callForChargeService.getPaidCallsByOwnerAndProprety(ownerId, propertyId, pageable);
    }

    @GetMapping("/kpi")
    public ResponseEntity<CallForChargeKpiResponse> getKpis(
            @RequestParam(required = false) Long promoterId,
            @RequestParam(required = false) Long propertyId) {

        if (promoterId != null) {
            return ResponseEntity.ok(callForChargeService.getKpiByPromoter(promoterId));
        } else if (propertyId != null) {
            return ResponseEntity.ok(callForChargeService.getKpiByParentProperty(propertyId));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<CallForCharge> updateStatus(
            @PathVariable("id") Long callId,
            @RequestParam("status") PaymentCallStatusEnum status) {

        CallForCharge updatedCall = callForChargeService.updateStatus(callId, status);
        return ResponseEntity.ok(updatedCall);
    }

    @GetMapping("/owner/{ownerId}/total-paid")
    public ResponseEntity<Double> getTotalPaidByOwner(@PathVariable Long ownerId) {
        double totalPaid = callForChargeService.getTotalPaidByOwner(ownerId);
        return ResponseEntity.ok(totalPaid);
    }

    @PostMapping("/{id}/file")
    public ResponseEntity<?> save(@ModelAttribute PaymentCallRequest request, @PathVariable Long id)
            throws java.io.IOException {
        try {

            String file = FileTraferUtil.handleFileUpload(request.getFile());

            request.setFileName(file);

            return ResponseEntity.ok(callForChargeService.addFile(id, request));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Échec d'enregistrement : " + e.getMessage());
        }
    }
            
            
            /**
     * Exemple :
     * PUT /api/call-for-charges/10/status?status=PAID
     */
    @PutMapping("/{id}/status-change")
    public ResponseEntity<CallForCharge> updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(
                callForChargeService.updateStatus(id, status)
        );
    }
}
