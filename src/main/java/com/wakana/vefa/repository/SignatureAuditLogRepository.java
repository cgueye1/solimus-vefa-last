package com.wakana.vefa.repository;

import com.wakana.vefa.model.SignatureAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SignatureAuditLogRepository extends JpaRepository<SignatureAuditLog, Long> {

    @Query("""
            SELECT a FROM SignatureAuditLog a
            WHERE (:keyword IS NULL OR :keyword = '' OR
                   LOWER(a.signerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(a.signerPhone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(a.projectName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(a.documentTitle) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY a.signedAt DESC
            """)
    Page<SignatureAuditLog> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT a FROM SignatureAuditLog a
            LEFT JOIN a.property parentProject
            WHERE (a.property.promoter.id = :promoterId OR parentProject.parentProperty.promoter.id = :promoterId)
            AND (:keyword IS NULL OR :keyword = '' OR
                 LOWER(a.signerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                 LOWER(a.signerPhone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                 LOWER(a.projectName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                 LOWER(a.documentTitle) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY a.signedAt DESC
            """)
    Page<SignatureAuditLog> searchForPromoter(@Param("promoterId") Long promoterId,
                                               @Param("keyword") String keyword,
                                               Pageable pageable);

    @Query("""
            SELECT a FROM SignatureAuditLog a
            WHERE a.signatory.id = :signatoryId
            AND (:keyword IS NULL OR :keyword = '' OR
                 LOWER(a.signerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                 LOWER(a.signerPhone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                 LOWER(a.projectName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                 LOWER(a.documentTitle) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY a.signedAt DESC
            """)
    Page<SignatureAuditLog> searchForSignatory(@Param("signatoryId") Long signatoryId,
                                                @Param("keyword") String keyword,
                                                Pageable pageable);
}