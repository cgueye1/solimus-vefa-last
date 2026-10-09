package com.wakana.vefa.services;
import java.util.List;
import com.wakana.vefa.model.SubscriptionPlanType;

public interface SubscriptionPlanTypeService {

    SubscriptionPlanType create(SubscriptionPlanType type);

    SubscriptionPlanType update(Long id, SubscriptionPlanType type);

    SubscriptionPlanType getById(Long id);

    List<SubscriptionPlanType> getAll();

    void delete(Long id);
}
