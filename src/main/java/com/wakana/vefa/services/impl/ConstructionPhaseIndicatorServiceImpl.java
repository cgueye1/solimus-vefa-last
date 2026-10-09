
package com.wakana.vefa.services.impl;
import com.wakana.vefa.model.ConstructionPhaseIndicator;
import com.wakana.vefa.repository.ConstructionPhaseIndicatorRepository;
import com.wakana.vefa.services.ConstructionPhaseIndicatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;



@Service
public class ConstructionPhaseIndicatorServiceImpl implements ConstructionPhaseIndicatorService {

    @Autowired
    private ConstructionPhaseIndicatorRepository indicatorRepository;

    @Override
    @Transactional
    public ConstructionPhaseIndicator updateProgress(Long indicatorId, int newProgressPercentage) {
        Optional<ConstructionPhaseIndicator> optionalIndicator = indicatorRepository.findById(indicatorId);

        if (optionalIndicator.isEmpty()) {
            throw new IllegalArgumentException("Indicator not found with ID: " + indicatorId);
        }

        ConstructionPhaseIndicator indicator = optionalIndicator.get();
        
        // Update the progress percentage and lastUpdated timestamp
        indicator.setProgressPercentage(newProgressPercentage);
        indicator.setLastUpdated( LocalDateTime.now()); // Automatically updates the timestamp
        
        return indicatorRepository.save(indicator);
    }
    
    
    @Override
    public List<ConstructionPhaseIndicator> getIndicatorsByProperty(Long propertyId) {
        return indicatorRepository.findByRealEstatePropertyId(propertyId);
    }
    
}


