package com.wakana.vefa.services;

import com.wakana.vefa.model.OwnerPropertyArea;
import java.util.List;

public interface OwnerPropertyAreaService {

    // Create a new subscription plan
    OwnerPropertyArea add(Long PId, Long userId, double area);

    void delete(Long id);

    void deleteByOwnerAndProperty(Long ownerId, Long propertyId);
}
