package com.wakana.vefa.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wakana.vefa.model.SubscriptionPlanType;

public interface SubscriptionPlanTypeRepository
        extends JpaRepository<SubscriptionPlanType, Long> {
            
                Optional<SubscriptionPlanType> findByLabel(String label);
                


}
