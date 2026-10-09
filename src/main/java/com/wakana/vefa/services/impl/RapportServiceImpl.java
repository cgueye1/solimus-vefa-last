
package com.wakana.vefa.services.impl;

import com.wakana.vefa.dto.RapportRequest;
import com.wakana.vefa.model.Rapport;
import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.repository.RapportRepository;
import com.wakana.vefa.repository.RealEstatePropertyRepository;
import com.wakana.vefa.services.RapportService;
import com.wakana.vefa.util.XssSanitizer;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RapportServiceImpl implements RapportService {
    private final RealEstatePropertyRepository realEstatePropertyRepository;
    private final RapportRepository rapportRepository;

    @Override
    public Rapport save(RapportRequest request, String pdf) {
        RealEstateProperty property = realEstatePropertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid real estate property ID"));

        Rapport rapport = new Rapport();
        rapport.setDescription(XssSanitizer.clean(request.getDescription()));
        rapport.setTitre(XssSanitizer.clean(request.getTitre()));
        rapport.setPdf(pdf);
        rapport.setRealEstateProperty(property);
        rapport.setLastUpdated( LocalDateTime.now());

        return rapportRepository.save(rapport);
    }

    @Override
    public void delete(Long id) {
      
         rapportRepository.deleteById(id);
    }

    @Override
    public Page<Rapport> getAll(int page, int size,Long id) {
         PageRequest pageable = PageRequest.of(page, size);
        return rapportRepository.findAllByRealEstatePropertyIdOrderByLastUpdatedDesc(id, pageable );
    }
    
    
     @Override
    public Rapport toggleArchive(Long rapportId) {
        Rapport rapport = rapportRepository.findById(rapportId)
                .orElseThrow(() -> new RuntimeException("Rapport introuvable"));

        // toggle archive
        rapport.setArchived(!rapport.isArchived());
        rapport.setLastUpdated(LocalDateTime.now());

        return rapportRepository.save(rapport);
    }

}
