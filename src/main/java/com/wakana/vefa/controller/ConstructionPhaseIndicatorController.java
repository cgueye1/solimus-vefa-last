package com.wakana.vefa.controller;

import com.wakana.vefa.model.ConstructionPhaseIndicator;
import com.wakana.vefa.services.ConstructionPhaseIndicatorService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/indicators")
public class ConstructionPhaseIndicatorController {

    @Autowired
    private ConstructionPhaseIndicatorService indicatorService;

    @PutMapping("/update/{id}")
    public ResponseEntity<ConstructionPhaseIndicator> updateProgress(
        @PathVariable Long id,
        @RequestParam int progressPercentage) {
        // Call service to update the progress
        ConstructionPhaseIndicator updatedIndicator = indicatorService.updateProgress(id, progressPercentage);

        return ResponseEntity.ok(updatedIndicator);
    }
    
    
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<ConstructionPhaseIndicator>> getIndicatorsByProperty(@PathVariable Long propertyId) {
        List<ConstructionPhaseIndicator> indicators = indicatorService.getIndicatorsByProperty(propertyId);
        return ResponseEntity.ok(indicators);
    }
}
