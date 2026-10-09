package com.wakana.vefa.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import com.wakana.vefa.dto.PaymentHistoryRequest;
import com.wakana.vefa.dto.response.PaymentHistoryResponse;
import com.wakana.vefa.services.PaymentHistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments-historics")
@RequiredArgsConstructor
public class PaymentHistoryController {

    private final PaymentHistoryService service;

    // Initiation du paiement
    @PostMapping
    public void create(@RequestBody PaymentHistoryRequest request) {
        service.createHistory(request);
    }

    // Validation du paiement
    @PutMapping("/validate")
    public void validate(@RequestParam Long userId,
            @RequestParam Long planId) {

        service.markAsPaid(userId, planId);
    }

    // Listing paginé + recherche
    @GetMapping
    public Page<PaymentHistoryResponse> list(

            @RequestParam(required = false) String keyword,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return service.list(pageable, keyword);
    }
}
