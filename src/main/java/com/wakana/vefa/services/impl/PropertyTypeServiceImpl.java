package com.wakana.vefa.services.impl;
import com.wakana.vefa.model.PropertyType;
import com.wakana.vefa.repository.PropertyTypeRepository;
import com.wakana.vefa.services.PropertyTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyTypeServiceImpl implements PropertyTypeService {

    private final PropertyTypeRepository propertyTypeRepository;

    @Override
    public PropertyType create(PropertyType propertyType) {
        return propertyTypeRepository.save(propertyType);
    }

    @Override
    public PropertyType update(Long id, PropertyType propertyType) {
        PropertyType existingPropertyType = propertyTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PropertyType not found"));
        existingPropertyType.setTypeName(propertyType.getTypeName());
        return propertyTypeRepository.save(existingPropertyType);
    }

    @Override
    public void delete(Long id) {
        propertyTypeRepository.deleteById(id);
    }

    @Override
    public List<PropertyType> getAll() {
        return propertyTypeRepository.findAll();
    }

    @Override
    public PropertyType getById(Long id) {
        return propertyTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PropertyType not found"));
    }
}
