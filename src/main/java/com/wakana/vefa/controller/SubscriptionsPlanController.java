
package com.wakana.vefa.controller;

import org.springframework.web.bind.annotation.*;

import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.model.SubscriptionPlans;
import com.wakana.vefa.services.SubscriptionsPlanService;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions-plans")
public class SubscriptionsPlanController {

    private final SubscriptionsPlanService subscriptionPlanService;

    public SubscriptionsPlanController(SubscriptionsPlanService subscriptionPlanService) {
        this.subscriptionPlanService = subscriptionPlanService;
    }

    @PostMapping
    public SubscriptionPlans createPlan(@RequestBody SubscriptionPlans plan) {
        return subscriptionPlanService.createPlan(
                plan.getName(),
                plan.getLabel(),
                plan.getDescription(),
                plan.getTotalCost(),
                plan.getInstallmentCount(),
                plan.getProjectLimit(),
                plan.isUnlimitedProjects(),
                plan.getYearlyDiscountRate(), plan.getId());
    }

    @GetMapping
    public List<SubscriptionPlans> getAllPlans() {
        return subscriptionPlanService.getAllPlans();
    }

    @GetMapping("/{id}")
    public SubscriptionPlans getPlanById(@PathVariable Long id) {
        return subscriptionPlanService.getPlanById(id);
    }

    @GetMapping("/name/{name}")
    public List<SubscriptionPlans> getPlansByName(@PathVariable ProfilEnum name) {
        return subscriptionPlanService.getPlansByName(name);
    }

    @PutMapping("/{id}")
    public void updatePlan(@PathVariable Long id, @RequestBody SubscriptionPlans plan) {
        subscriptionPlanService.updatePlan(
                id,
                plan.getLabel(),
                plan.getDescription(),
                plan.getTotalCost(),
                plan.getInstallmentCount(),
                plan.getProjectLimit(),
                plan.isUnlimitedProjects(),
                plan.getYearlyDiscountRate());
    }

    @DeleteMapping("/{id}")
    public void deletePlan(@PathVariable Long id) {
        subscriptionPlanService.deletePlan(id);
    }
}
