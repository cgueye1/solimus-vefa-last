package com.wakana.vefa.repository;

import com.wakana.vefa.model.InformationSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InformationSheetRepository extends JpaRepository<InformationSheet, Long> {
    List<InformationSheet> findByNotaryId(Long notaryId);
}
