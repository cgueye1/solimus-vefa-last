package com.wakana.vefa.repository;

import com.wakana.vefa.model.OwnerPropertyArea;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OwnerPropertyAreaRepository extends JpaRepository<OwnerPropertyArea, Long> {
    List<OwnerPropertyArea> findByRealEstatePropertyId(Long realEstatePropertyId);

    boolean existsByOwnerIdAndRealEstatePropertyId(Long ownerId, Long realEstatePropertyId);

    Optional<OwnerPropertyArea> findByOwnerIdAndRealEstatePropertyId(Long ownerId, Long propertyId);

    void deleteByOwnerIdAndRealEstatePropertyId(Long ownerId, Long propertyId);
}
