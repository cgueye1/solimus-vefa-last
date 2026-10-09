package com.wakana.vefa.repository;

import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.model.Work;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkRepository extends JpaRepository<Work, Long> {
    Page<Work> findByRealEstatePropertyId(Long propertyId, Pageable pageable);

    List<Work> findByRealEstateProperty(RealEstateProperty property);

    @Query("SELECT w FROM Work w WHERE w.realEstateProperty.id = :propertyId " +
            "AND FUNCTION('DATE_FORMAT', w.startDate, '%Y-%m') = :month")
    Page<Work> findByRealEstatePropertyIdAndMonth(
            @Param("propertyId") Long propertyId,
            @Param("month") String month,
            Pageable pageable);

}
