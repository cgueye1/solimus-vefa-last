package com.wakana.vefa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wakana.vefa.enums.PaymentCallStatusEnum;
import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.model.Subscriptions;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SubscriptionsRepository extends JpaRepository<Subscriptions, Long> {

        // Optional<Subscriptions> findByUserId(Long userId);

        List<Subscriptions> findByActiveTrueAndStatus(PaymentCallStatusEnum status);

        Page<Subscriptions> findByUserIdOrderByEndDateDesc(Long userId, Pageable pageable);

        Page<Subscriptions> findByUserIdAndStatusOrderByEndDateDesc(Long userId, PaymentCallStatusEnum status,
                        Pageable pageable);

        boolean existsByUserIdAndActiveTrue(Long userId);

        boolean existsByUserIdAndSubscriptionPlans_NameAndActiveTrue(
                        Long userId,
                        ProfilEnum name);

        // Optional<Subscriptions> findByUserIdAndActiveTrue(Long userId);

        Optional<Subscriptions> findByUserIdAndSubscriptionPlans_NameAndActiveTrue(
                        Long userId,
                        ProfilEnum name);

        @Query("SELECT MONTH(s.startDate) AS month, COUNT(s) AS total " +
                        "FROM Subscription s " +
                        "WHERE YEAR(s.startDate) = :year " +
                        "GROUP BY MONTH(s.startDate)")
        List<Object[]> countSubscriptionsByMonth(@Param("year") int year);

        @Query("SELECT s.subscriptionPlan.name, COUNT(s) FROM Subscription s GROUP BY s.subscriptionPlan.name")
        List<Object[]> countSubscriptionsByPlan();

        @Query("SELECT COUNT(s) FROM Subscription s")
        long countAllSubscriptions();

        // 🔹 1) Tous les abonnements d’un user
        List<Subscriptions> findByUserId(Long userId);

        // 🔹 2) Abonnement d’un user par nom de plan
        Optional<Subscriptions> findByUserIdAndSubscriptionPlans_Name(
                        Long userId,
                        ProfilEnum name);

        Optional<Subscriptions> findByUserIdAndSubscriptionPlans_Id(
                        Long userId,
                        Long planId);
        /// Page<Subscription> findByUserIdOrderByEndDateDesc(Long userId, Pageable
        /// pageable);

}