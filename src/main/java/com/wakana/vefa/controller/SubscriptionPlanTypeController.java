package com.wakana.vefa.controller;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.wakana.vefa.model.SubscriptionPlanType;
import com.wakana.vefa.services.SubscriptionPlanTypeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/subscription-plan-types")
@RequiredArgsConstructor
public class SubscriptionPlanTypeController {

    private final SubscriptionPlanTypeService service;

    @PostMapping
    public ResponseEntity<SubscriptionPlanType> create(
            @RequestBody SubscriptionPlanType type) {
        return ResponseEntity.ok(service.create(type));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionPlanType> update(
            @PathVariable Long id,
            @RequestBody SubscriptionPlanType type) {
        return ResponseEntity.ok(service.update(id, type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionPlanType> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionPlanType>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
