package com.wakana.vefa.services;




import java.util.List;

import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.model.SubscriptionPlans;


public interface SubscriptionsPlanService {
    
    SubscriptionPlans createPlan(
       ProfilEnum name,
        String label,
        String description,
        double totalCost,
        int installmentCount,
        int projectLimit,
        boolean unlimitedProjects,
        double yearlyDiscountRate,
        Long typeId
    );

    SubscriptionPlans getPlanById(Long id);

    List<SubscriptionPlans> getPlansByName(ProfilEnum name);

    List<SubscriptionPlans> getAllPlans();

    void updatePlan(
        Long id,
        String label,
        String description,
        double totalCost,
        int installmentCount,
        int projectLimit,
        boolean unlimitedProjects,
        double yearlyDiscountRate
    );

    void deletePlan(Long id);
}
