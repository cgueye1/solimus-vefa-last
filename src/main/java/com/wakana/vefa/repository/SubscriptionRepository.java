package com.wakana.vefa.repository;

import com.wakana.vefa.enums.PaymentCallStatusEnum;
import com.wakana.vefa.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

        Optional<Subscription> findByUserId(Long userId);

        List<Subscription> findByActiveTrueAndStatus(PaymentCallStatusEnum status);

        Page<Subscription> findByUserIdOrderByEndDateDesc(Long userId, Pageable pageable);

        Page<Subscription> findByUserIdAndStatusOrderByEndDateDesc(Long userId, PaymentCallStatusEnum status,
                        Pageable pageable);

        @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
                        "FROM Subscription s " +
                        "WHERE s.user.id = :userId " +
                        "AND s.active = true " +
                        "AND s.status = :status")
        boolean existsActivePaidSubscription(@Param("userId") Long userId,
                        @Param("status") PaymentCallStatusEnum status);

        @Query("""
                            SELECT MONTH(s.dateInvoice), SUM(s.paidAmount)
                            FROM Subscription s
                            WHERE s.dateInvoice IS NOT NULL
                              AND YEAR(s.dateInvoice) = :year
                            GROUP BY MONTH(s.dateInvoice)
                            ORDER BY MONTH(s.dateInvoice)
                        """)
        List<Object[]> sumRevenueGroupedByMonth(@Param("year") int year);

}