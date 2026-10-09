package com.wakana.vefa.services;

import com.wakana.vefa.model.ConstructionPhaseIndicator;
import java.util.List;

public interface ConstructionPhaseIndicatorService {
    ConstructionPhaseIndicator updateProgress(Long indicatorId, int newProgressPercentage);
    List<ConstructionPhaseIndicator> getIndicatorsByProperty(Long propertyId);

}
