package com.wakana.vefa.services.impl;

import org.springframework.stereotype.Service;

import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.model.SubscriptionPlanType;
import com.wakana.vefa.model.SubscriptionPlans;
import com.wakana.vefa.repository.SubscriptionPlanTypeRepository;
import com.wakana.vefa.repository.SubscriptionsPlanRepository;
import com.wakana.vefa.services.SubscriptionsPlanService;

import java.util.List;

@Service
public class SubscriptionsPlanServiceImpl implements SubscriptionsPlanService {

    private final SubscriptionsPlanRepository subscriptionPlanRepository;
    private final SubscriptionPlanTypeRepository subscriptionPlanTypeRepository;

    public SubscriptionsPlanServiceImpl(SubscriptionsPlanRepository subscriptionPlanRepository,
            SubscriptionPlanTypeRepository subscriptionPlanTypeRepository) {
        this.subscriptionPlanRepository = subscriptionPlanRepository;

        this.subscriptionPlanTypeRepository = subscriptionPlanTypeRepository;
    }

    @Override
    public SubscriptionPlans createPlan(
            ProfilEnum name,
            String label,
            String description,
            double totalCost,
            int installmentCount,
            int projectLimit,
            boolean unlimitedProjects,
            double yearlyDiscountRate,
            Long typeId) {

        SubscriptionPlanType planType = subscriptionPlanTypeRepository.findById(typeId)
                .orElseThrow(() -> new RuntimeException("Subscription plan type not found with id: " + typeId));

        SubscriptionPlans plan = new SubscriptionPlans();
        plan.setName(name);
        plan.setLabel(label);
        plan.setDescription(description);
        plan.setTotalCost(totalCost);
        plan.setInstallmentCount(installmentCount);
        plan.setProjectLimit(projectLimit);
        plan.setUnlimitedProjects(unlimitedProjects);
        plan.setYearlyDiscountRate(yearlyDiscountRate);

        // 🔗 Relation avec le type de plan
        plan.setPlanType(planType);

        return subscriptionPlanRepository.save(plan);
    }

    @Override
    public SubscriptionPlans getPlanById(Long id) {
        return subscriptionPlanRepository.findById(id).orElse(null);
    }

    @Override
    public List<SubscriptionPlans> getPlansByName(ProfilEnum name) {
        return subscriptionPlanRepository.findAllByName(name);
    }

    @Override
    public List<SubscriptionPlans> getAllPlans() {
        return subscriptionPlanRepository.findAll();
    }

    @Override
    public void updatePlan(Long id, String label, String description, double totalCost, int installmentCount,
            int projectLimit, boolean unlimitedProjects, double yearlyDiscountRate) {

        subscriptionPlanRepository.findById(id).ifPresent(plan -> {
            plan.setLabel(label);
            plan.setDescription(description);
            plan.setTotalCost(totalCost);
            plan.setInstallmentCount(installmentCount);
            plan.setProjectLimit(projectLimit);
            plan.setUnlimitedProjects(unlimitedProjects);
            plan.setYearlyDiscountRate(yearlyDiscountRate);
            subscriptionPlanRepository.save(plan);
        });
    }

    @Override
    public void deletePlan(Long id) {
        subscriptionPlanRepository.deleteById(id);
    }
}
