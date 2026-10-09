package com.wakana.vefa.repository;
import com.wakana.vefa.model.Docs;
import com.wakana.vefa.model.SignatoryStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocsRepository extends JpaRepository<Docs, Long> {

    List<Docs> findByRealEstatePropertyId(Long propertyId);
    
 
    
    @Query("SELECT d FROM Docs d LEFT JOIN FETCH d.signatories WHERE d.id = :id")
    Optional<Docs> findByIdWithSignatories(@Param("id") Long id);
    
    @Query("SELECT s FROM SignatoryStatus s WHERE s.document.id = :documentId AND s.signatory.id = :userId")
    Optional<SignatoryStatus> findSignatoryStatus(@Param("documentId") Long documentId, @Param("userId") Long userId);


}