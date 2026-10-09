package com.wakana.vefa.controller;

import com.wakana.vefa.enums.PaymentCallStatusEnum;
import com.wakana.vefa.model.Subscription;
import com.wakana.vefa.model.User;
import com.wakana.vefa.services.SubscriptionService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    @Value("${angular.url.success}")
    private String angularSuccessUrl;

    @Value("${angular.url.failed}")
    private String angularFailedUrl;

    @PostMapping("/create")
    public ResponseEntity<Subscription> createSubscription(@RequestBody User user, @RequestParam Long planId) {
        Subscription subscription = subscriptionService.createSubscription(user, planId);
        return ResponseEntity.ok(subscription);
    }

    @GetMapping("/{subscriptionId}/{installmentCount}/process-payment")
    public ResponseEntity<Void> processPayment(
            @PathVariable Long subscriptionId,
            @PathVariable int installmentCount,
            @RequestParam(required = false) String num_transaction_from_gu,
            @RequestParam(required = false) String num_command,
            @RequestParam(required = false) Double amount,
            @RequestParam(required = false) String errorCode) {

        // Logs pour debug
        System.out.println("Transaction: " + num_transaction_from_gu);
        System.out.println("Commande: " + num_command);
        System.out.println("Montant: " + amount);
        System.out.println("Code erreur: " + errorCode);

        boolean paymentOk = "200".equals(errorCode.toString());

        String angularUrl;
        if (paymentOk) {
            subscriptionService.processPayment(subscriptionId, installmentCount);
            angularUrl = angularSuccessUrl;
        } else {
            angularUrl = angularFailedUrl + "&errorCode=" + errorCode;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(angularUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND); // 302 redirect
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<Void> cancelSubscription(@PathVariable Long id) {
        subscriptionService.cancelSubscription(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/status")
    public ResponseEntity<Boolean> isSubscriptionActive(@PathVariable Long userId) {
        boolean isActive = subscriptionService.isSubscriptionActive(userId);
        return ResponseEntity.ok(isActive);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionByUserId(userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Subscription>> getUserSubscriptionsOrdered(@PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Subscription> subscriptions = subscriptionService.getUserSubscriptionsOrderedByEndDate(userId, pageable);
        return ResponseEntity.ok(subscriptions);
    }

    //// admin
    @GetMapping
    public ResponseEntity<Page<Subscription>> getSubscriptions(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) PaymentCallStatusEnum status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Subscription> subscriptions = subscriptionService.getSubscriptions(userId, status, pageable);
        return ResponseEntity.ok(subscriptions);

    }

    @GetMapping("/check/{userId}")
    public ResponseEntity<Boolean> checkActivePaidSubscription(@PathVariable Long userId) {
        boolean hasActivePaid = subscriptionService.hasActivePaidSubscription(userId);
        return ResponseEntity.ok(hasActivePaid);
    }

    @GetMapping("/{id}/is-paid")
    public boolean isSubscriptionPaid(@PathVariable("id") Long id) {
        return subscriptionService.isSubscriptionPaid(id);
    }

    @GetMapping("/dashboard/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueByMonth(
            @RequestParam(required = false) Integer year) {

        return ResponseEntity.ok(
                subscriptionService.getRevenueByMonth(year));
    }

}
