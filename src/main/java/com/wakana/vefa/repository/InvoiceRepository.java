package com.wakana.vefa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.model.Invoice;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
        Page<Invoice> findByUserId(Long userId, Pageable pageable);

        @Query("SELECT MONTH(i.createdAt) AS month, SUM(i.amount) AS total " +
                        "FROM Invoice i " +
                        "WHERE YEAR(i.createdAt) = :year AND i.paid = true " +
                        "GROUP BY MONTH(i.createdAt)")
        List<Object[]> getMonthlyRevenues(@Param("year") int year);

        Page<Invoice> findAllByOrderByCreatedAtDesc(Pageable pageable);

        Page<Invoice> findBySubscription_User_IdAndSubscription_SubscriptionPlans_Name(
                        Long userId,
                        ProfilEnum planName,
                        Pageable pageable);
        Optional<Invoice> findTopBySubscription_IdOrderByIdDesc(Long subscriptionId);
}
