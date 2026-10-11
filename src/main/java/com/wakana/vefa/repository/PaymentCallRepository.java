package com.wakana.vefa.repository;

import com.wakana.vefa.enums.PaymentCallStatusEnum;
import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.model.PaymentCall;
import com.wakana.vefa.model.RealEstateProperty;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentCallRepository extends JpaRepository<PaymentCall, Long> {

        List<PaymentCall> findByRealEstatePropertyRecipientIdOrderByExpectedDateAsc(Long recipientId);

    List<PaymentCall> findByRealEstatePropertyId(Long realEstatePropertyId);

    List<PaymentCall> findByRealEstatePropertyIdOrderByCreatedAtDesc(Long realEstatePropertyId);

    @Query(value = "SELECT * FROM payment_calls p WHERE " +
            "DATEDIFF(STR_TO_DATE(p.expected_date, '%d-%m-%Y'), CURDATE()) <= 2 " +
            "AND p.status = 'PENDING'", nativeQuery = true)
    List<PaymentCall> findDueOrOverdueCalls();

    @Query(value = "SELECT * FROM payment_calls p WHERE p.real_estate_property_id = :propertyId " +
            "AND STR_TO_DATE(p.expected_date, '%d-%m-%Y') < CURDATE()", nativeQuery = true)
    List<PaymentCall> findOverdueByRealEstatePropertyId(Long propertyId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PaymentCall p WHERE p.realEstateProperty.id = :propertyId")
    void deleteByRealEstatePropertyId(@Param("propertyId") Long propertyId);

    @Query("SELECT SUM(p.amount) FROM PaymentCall p WHERE p.realEstateProperty.promoter.id = :promoterId")
    Double getTotalAmountByPromoter(@Param("promoterId") Long promoterId);

    @Query("SELECT SUM(p.amount) FROM PaymentCall p WHERE p.realEstateProperty.promoter.id = :promoterId AND p.status = 'PAID'")
    Double getPaidAmountByPromoter(@Param("promoterId") Long promoterId);

    @Query("SELECT SUM(p.amount) FROM PaymentCall p WHERE p.realEstateProperty.promoter.id = :promoterId AND p.status <> 'PAID'")
    Double getUnpaidAmountByPromoter(@Param("promoterId") Long promoterId);

    @Query("SELECT SUM(p.amount) FROM PaymentCall p WHERE p.realEstateProperty.parentProperty.id = :propertyId")
    Double getTotalAmountByParentProperty(@Param("propertyId") Long propertyId);

    @Query("SELECT SUM(p.amount) FROM PaymentCall p WHERE p.realEstateProperty.parentProperty.id = :propertyId AND p.status = 'PAID'")
    Double getPaidAmountByParentProperty(@Param("propertyId") Long propertyId);

    @Query("SELECT SUM(p.amount) FROM PaymentCall p WHERE p.realEstateProperty.parentProperty.id = :propertyId AND p.status <> 'PAID'")
    Double getUnpaidAmountByParentProperty(@Param("propertyId") Long propertyId);

    /// apply. sub

    @Query("""
                SELECT SUM(p.amount)
                FROM PaymentCall p
                WHERE p.realEstateProperty.promoter.id = :promoterId
                  AND p.realEstateProperty.parentProperty.subscription.subscriptionPlans.name  = :plan
            """)
    Double getTotalAmountByPromoterAndPlan(@Param("promoterId") Long promoterId,
            @Param("plan") ProfilEnum plan);

    @Query("""
                SELECT SUM(p.amount)
                FROM PaymentCall p
                WHERE p.realEstateProperty.promoter.id = :promoterId
                  AND p.realEstateProperty.parentProperty.subscription.subscriptionPlans.name  = :plan
                  AND p.status = 'PAID'
            """)
    Double getPaidAmountByPromoterAndPlan(@Param("promoterId") Long promoterId,
            @Param("plan") ProfilEnum plan);

    @Query("""
                SELECT SUM(p.amount)
                FROM PaymentCall p
                WHERE p.realEstateProperty.promoter.id = :promoterId
                  AND p.realEstateProperty.parentProperty.subscription.subscriptionPlans.name = :plan
                  AND p.status <> 'PAID'
            """)
    Double getUnpaidAmountByPromoterAndPlan(@Param("promoterId") Long promoterId,
            @Param("plan") ProfilEnum plan);

    @Query("""
                SELECT SUM(p.amount)
                FROM PaymentCall p
                WHERE p.realEstateProperty.parentProperty.id = :propertyId
                  AND p.realEstateProperty.parentProperty.subscription.subscriptionPlans.name  = :plan
            """)
    Double getTotalAmountByParentPropertyAndPlan(@Param("propertyId") Long propertyId,
            @Param("plan") ProfilEnum plan);

    @Query("""
                SELECT SUM(p.amount)
                FROM PaymentCall p
                WHERE p.realEstateProperty.parentProperty.id = :propertyId
                  AND p.realEstateProperty.parentProperty.subscription.subscriptionPlans.name  = :plan
                  AND p.status = 'PAID'
            """)
    Double getPaidAmountByParentPropertyAndPlan(@Param("propertyId") Long propertyId,
            @Param("plan") ProfilEnum plan);

    @Query("""
                SELECT SUM(p.amount)
                FROM PaymentCall p
                WHERE p.realEstateProperty.parentProperty.id = :propertyId
                  AND p.realEstateProperty.parentProperty.subscription.subscriptionPlans.name  = :plan
                  AND p.status <> 'PAID'
            """)
    Double getUnpaidAmountByParentPropertyAndPlan(@Param("propertyId") Long propertyId,
            @Param("plan") ProfilEnum plan);

    ///
    Optional<PaymentCall> findTopByRealEstatePropertyOrderByCreatedAtDesc(
            RealEstateProperty property);

    List<PaymentCall> findByRealEstatePropertyIdAndStatus(
            Long propertyId,
            PaymentCallStatusEnum status);

    // V2

    // Tous les appels de fonds des lots d'un projet
    @Query("SELECT pc FROM PaymentCall pc WHERE pc.realEstateProperty.parentProperty.id = :projectId")
    List<PaymentCall> findAllByProjectId(@Param("projectId") Long projectId);

    // Appels par statut
    @Query("SELECT pc FROM PaymentCall pc WHERE pc.realEstateProperty.parentProperty.id = :projectId AND pc.status = :status")
    List<PaymentCall> findByProjectIdAndStatus(@Param("projectId") Long projectId,
            @Param("status") PaymentCallStatusEnum status);

    // Somme encaissée
    @Query("SELECT COALESCE(SUM(pc.amount), 0) FROM PaymentCall pc WHERE pc.realEstateProperty.parentProperty.id = :projectId AND pc.status = 'PAID'")
    double sumEncaissementsRecus(@Param("projectId") Long projectId);

    // Somme prévue
    @Query("SELECT COALESCE(SUM(pc.amount), 0) FROM PaymentCall pc WHERE pc.realEstateProperty.parentProperty.id = :projectId")
    double sumEncaissementsPrevus(@Param("projectId") Long projectId);

    List<PaymentCall> findByStatus(PaymentCallStatusEnum status);

}
