package com.wakana.vefa.services.impl;



import java.util.List;

import org.springframework.stereotype.Service;

import com.wakana.vefa.model.SubscriptionPlanType;
import com.wakana.vefa.repository.SubscriptionPlanTypeRepository;
import com.wakana.vefa.services.SubscriptionPlanTypeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanTypeServiceImpl implements SubscriptionPlanTypeService {

    private final SubscriptionPlanTypeRepository repository;

    @Override
    public SubscriptionPlanType create(SubscriptionPlanType type) {
        return repository.save(type);
    }

    @Override
    public SubscriptionPlanType update(Long id, SubscriptionPlanType type) {
        SubscriptionPlanType existing = getById(id);
        existing.setLabel(type.getLabel());
        existing.setLevel(type.getLevel());
        return repository.save(existing);
    }

    @Override
    public SubscriptionPlanType getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Plan type not found with id: " + id));
    }

    @Override
    public List<SubscriptionPlanType> getAll() {
        return repository.findAll();
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
