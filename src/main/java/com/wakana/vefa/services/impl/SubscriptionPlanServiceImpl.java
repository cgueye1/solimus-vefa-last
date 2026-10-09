package com.wakana.vefa.services.impl;
import com.wakana.vefa.enums.SubscriptionPlanEnum;
import com.wakana.vefa.model.SubscriptionPlan;
import com.wakana.vefa.repository.SubscriptionPlanRepository;
import com.wakana.vefa.services.SubscriptionPlanService;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;



    @Override
    public SubscriptionPlan createPlan(SubscriptionPlanEnum name, double totalCost, int installmentCount) {
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setName(name);
        plan.setTotalCost(totalCost);
        plan.setInstallmentCount(installmentCount);
        return subscriptionPlanRepository.save(plan);
    }

    @Override
    public SubscriptionPlan getPlanById(Long id) {
        return subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found with id: " + id));
    }

    @Override
    public List<SubscriptionPlan> getAllPlans() {
        return subscriptionPlanRepository.findAll();
    }

    @Override
    public void updatePlan(Long id, double totalCost, int installmentCount) {
        SubscriptionPlan plan = getPlanById(id);

        plan.setTotalCost(totalCost);
        plan.setInstallmentCount(installmentCount);
        subscriptionPlanRepository.save(plan);
    }

    @Override
    public void deletePlan(Long id) {
        SubscriptionPlan plan = getPlanById(id);
        subscriptionPlanRepository.delete(plan);
    }

    @Override
    public SubscriptionPlan getPlanByName(SubscriptionPlanEnum name) {
        return subscriptionPlanRepository.findByName(name).orElse(null);

    }
    
    
    
}
