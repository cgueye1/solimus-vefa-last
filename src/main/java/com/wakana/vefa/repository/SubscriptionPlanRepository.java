package com.wakana.vefa.repository;
import com.wakana.vefa.enums.SubscriptionPlanEnum;
import com.wakana.vefa.model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    Optional<SubscriptionPlan> findByName(SubscriptionPlanEnum name);
    
}
