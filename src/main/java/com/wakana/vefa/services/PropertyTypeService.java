package com.wakana.vefa.services;

import com.wakana.vefa.model.PropertyType;

import java.util.List;

public interface PropertyTypeService {
    PropertyType create(PropertyType propertyType);
    PropertyType update(Long id, PropertyType propertyType);
    void delete(Long id);
    List<PropertyType> getAll();
    PropertyType getById(Long id);
}
