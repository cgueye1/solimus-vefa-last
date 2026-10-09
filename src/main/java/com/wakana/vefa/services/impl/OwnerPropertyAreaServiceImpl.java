package com.wakana.vefa.services.impl;

import org.springframework.stereotype.Service;

import com.wakana.vefa.model.OwnerPropertyArea;
import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.OwnerPropertyAreaRepository;
import com.wakana.vefa.repository.RealEstatePropertyRepository;
import com.wakana.vefa.repository.UserRepository;
import com.wakana.vefa.services.OwnerPropertyAreaService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OwnerPropertyAreaServiceImpl implements OwnerPropertyAreaService {
  private final UserRepository userRepository;
  private final RealEstatePropertyRepository realEstatePropertyRepository;
  private final OwnerPropertyAreaRepository ownerPropertyAreaRepository;

  @Override
  public OwnerPropertyArea add(Long PId, Long userId, double area) {
    RealEstateProperty existingProperty = realEstatePropertyRepository.findById(PId)
        .orElseThrow(() -> new RuntimeException("Property not found with id: " + PId));

    User owner = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("Owner not found with id: " + userId));

    // Vérifier si l'association existe déjà
    boolean exists = ownerPropertyAreaRepository.existsByOwnerIdAndRealEstatePropertyId(userId, PId);
    if (exists) {
      throw new RuntimeException("An OwnerPropertyArea already exists for this owner and property.");
    }

    OwnerPropertyArea ownerPropertyArea = new OwnerPropertyArea();
    ownerPropertyArea.setAreaInSquareMeters(area);
    ownerPropertyArea.setOwner(owner);
    ownerPropertyArea.setRealEstateProperty(existingProperty);

    return ownerPropertyAreaRepository.save(ownerPropertyArea);
  }

  @Override
  public void delete(Long id) {

    ownerPropertyAreaRepository.deleteById(id);
  }

  @Override
  public void deleteByOwnerAndProperty(Long ownerId, Long propertyId) {
    OwnerPropertyArea opa = ownerPropertyAreaRepository
        .findByOwnerIdAndRealEstatePropertyId(ownerId, propertyId)
        .orElseThrow(() -> new RuntimeException(
            "OwnerPropertyArea not found for owner " + ownerId + " and property " + propertyId));

    ownerPropertyAreaRepository.delete(opa);
  }

}
