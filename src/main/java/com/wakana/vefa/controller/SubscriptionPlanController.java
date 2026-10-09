package com.wakana.vefa.controller;
import com.wakana.vefa.model.SubscriptionPlan;
import com.wakana.vefa.services.SubscriptionPlanService;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@Hidden
@RestController
@RequestMapping("/api/subscription-plans")
@RequiredArgsConstructor
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

 


    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionPlan> getPlanById(@PathVariable Long id) {
        SubscriptionPlan plan = subscriptionPlanService.getPlanById(id);
        return ResponseEntity.ok(plan);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SubscriptionPlan>> getAllPlans() {
        List<SubscriptionPlan> plans = subscriptionPlanService.getAllPlans();
        return ResponseEntity.ok(plans);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<SubscriptionPlan> updatePlan(@PathVariable Long id, @RequestParam double totalCost, @RequestParam int installmentCount) {
        subscriptionPlanService.updatePlan(id, totalCost, installmentCount);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        subscriptionPlanService.deletePlan(id);
        return ResponseEntity.ok().build();
    }
}
