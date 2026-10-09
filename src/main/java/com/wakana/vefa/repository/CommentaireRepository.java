package com.wakana.vefa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.wakana.vefa.model.Commentaire;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;

public interface CommentaireRepository extends JpaRepository<Commentaire, Long> {
    Page<Commentaire> findByPropertyIdAndParentIsNullOrderByDateDesc(Long propertyId, Pageable pageable);
    @Modifying
    @Query("DELETE FROM Commentaire c WHERE c.property.id = :propertyId")
    void deleteByPropertyId(@Param("propertyId") Long actuId);
}
