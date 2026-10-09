package com.wakana.vefa.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.wakana.vefa.model.Rapport;
import com.wakana.vefa.model.Rating;
import com.wakana.vefa.model.RealEstateProperty;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;




public interface  RatingRepository extends JpaRepository<Rating, Long> {
    



}
