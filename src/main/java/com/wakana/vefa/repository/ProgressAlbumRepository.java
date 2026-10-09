package com.wakana.vefa.repository;

import com.wakana.vefa.model.ProgressAlbum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgressAlbumRepository extends JpaRepository<ProgressAlbum, Long> {
    List<ProgressAlbum> findByRealEstatePropertyId(Long realEstatePropertyId);
}
