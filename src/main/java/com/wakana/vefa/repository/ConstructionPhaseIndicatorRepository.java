package com.wakana.vefa.repository;

import com.wakana.vefa.model.ConstructionPhaseIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConstructionPhaseIndicatorRepository extends JpaRepository<ConstructionPhaseIndicator, Long> {
    List<ConstructionPhaseIndicator> findByRealEstatePropertyId(Long realEstatePropertyId);
    
}
