package com.wakana.vefa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.model.SubscriptionPlans;

import java.util.List;
import java.util.Optional;

public interface SubscriptionsPlanRepository extends JpaRepository<SubscriptionPlans, Long> {
    // Optional<SubscriptionPlan> findByName(SubscriptionPlanEnum name);
    List<SubscriptionPlans> findAllByName(ProfilEnum name);

}
