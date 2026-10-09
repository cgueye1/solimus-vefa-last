package com.wakana.vefa.controller;
import com.wakana.vefa.model.PropertyType;
import com.wakana.vefa.services.PropertyTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/property-types")
@RequiredArgsConstructor
public class PropertyTypeController {

    private final PropertyTypeService propertyTypeService;

    @PostMapping("/save")
    public ResponseEntity<PropertyType> create(@RequestBody PropertyType propertyType) {
        PropertyType createdPropertyType = propertyTypeService.create(propertyType);
        return ResponseEntity.status(201).body(createdPropertyType);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyType> update(@PathVariable Long id, @RequestBody PropertyType propertyType) {
        PropertyType updatedPropertyType = propertyTypeService.update(id, propertyType);
        return ResponseEntity.ok(updatedPropertyType);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        propertyTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<PropertyType>> getAll() {
        List<PropertyType> propertyTypes = propertyTypeService.getAll();
        return ResponseEntity.ok(propertyTypes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyType> getById(@PathVariable Long id) {
        PropertyType propertyType = propertyTypeService.getById(id);
        return ResponseEntity.ok(propertyType);
    }
}
