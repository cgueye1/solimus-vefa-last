package com.wakana.vefa.services;

import com.wakana.vefa.enums.SubscriptionPlanEnum;
import com.wakana.vefa.model.SubscriptionPlan;

import java.util.List;

public interface SubscriptionPlanService {
    
    // Create a new subscription plan
    SubscriptionPlan createPlan(SubscriptionPlanEnum name, double totalCost, int installmentCount);

    // Retrieve a subscription plan by its ID
    SubscriptionPlan getPlanById(Long id);

    // Retrieve a subscription plan by its name (enum)
    SubscriptionPlan getPlanByName(SubscriptionPlanEnum name);

    // Get all available subscription plans
    List<SubscriptionPlan> getAllPlans();

    // Update an existing subscription plan
    void updatePlan(Long id, double totalCost, int installmentCount);

    // Delete a subscription plan by its ID
    void deletePlan(Long id);
}
