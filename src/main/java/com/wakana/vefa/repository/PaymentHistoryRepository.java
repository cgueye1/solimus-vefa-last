package com.wakana.vefa.repository;



import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.wakana.vefa.model.PaymentHistory;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    @Query("""
        SELECT p FROM PaymentHistory p
        WHERE p.user.id = :userId
        AND p.plan.id = :planId
        ORDER BY p.createdAt DESC
    """)
    Optional<PaymentHistory> findLastByUserAndPlan(Long userId, Long planId);

    @Query("""
        SELECT p FROM PaymentHistory p
        WHERE (:keyword IS NULL OR
               LOWER(p.user.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(p.user.prenom) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    Page<PaymentHistory> search(Pageable pageable, String keyword);
}
