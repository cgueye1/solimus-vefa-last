package com.wakana.vefa.repository;

import com.wakana.vefa.enums.FeeEnum;
import com.wakana.vefa.enums.PaymentCallStatusEnum;
import com.wakana.vefa.model.CallForCharge;
import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.model.Work;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CallForChargeRepository extends JpaRepository<CallForCharge, Long> {
        Page<CallForCharge> findByOwnerIdAndStatusOrderByExpectedDateAsc(Long ownerId, PaymentCallStatusEnum status,
                        Pageable pageable);

        Page<CallForCharge> findByOwnerIdAndStatus(Long ownerId, PaymentCallStatusEnum status, Pageable pageable);

        Page<CallForCharge> findByOwnerIdAndRealEstatePropertyId(Long ownerId, Long realEstatePropertyId,
                        Pageable pageable);

        @Query(value = "SELECT cfc.* " +
                        "FROM call_for_charge cfc " +
                        "INNER JOIN (" +
                        "    SELECT real_estate_property_id, MIN(expected_date) AS min_expected_date " +
                        "    FROM call_for_charge " +
                        "    WHERE charge_owner_id = :ownerId " +
                        "    AND status = :status " +
                        "    GROUP BY real_estate_property_id" +
                        ") sub ON cfc.real_estate_property_id = sub.real_estate_property_id " +
                        "AND cfc.expected_date = sub.min_expected_date " +
                        "WHERE cfc.charge_owner_id = :ownerId " +
                        "AND cfc.status = :status", nativeQuery = true)
        List<CallForCharge> findEarliestPendingCallsByOwner(@Param("ownerId") Long ownerId,
                        @Param("status") String status);

        @Query("SELECT COALESCE(SUM(c.fee), 0) FROM CallForCharge c WHERE c.owner.id = :ownerId AND c.status = 'PENDING'")
        double getTotalPendingFeesByOwnerId(@Param("ownerId") Long ownerId);

        Page<CallForCharge> findByOwnerIdAndStatusAndRealEstatePropertyId(Long ownerId, PaymentCallStatusEnum status,
                        Long realEstatePropertyId, Pageable pageable);

        // dashbord
        // Cas 1 : Propriétés parent dont le promoteur correspond

        @Query("SELECT SUM(c.fee) FROM CallForCharge c WHERE c.realEstateProperty.promoter.id = :promoterId")
        Double getTotalFeeByPromoter(@Param("promoterId") Long promoterId);

        @Query("SELECT SUM(c.fee) FROM CallForCharge c WHERE c.realEstateProperty.parentProperty.promoter.id = :promoterId AND c.status = 'PAID'")
        Double getPaidFeeByPromoter(@Param("promoterId") Long promoterId);

        @Query("SELECT SUM(c.fee) FROM CallForCharge c WHERE c.realEstateProperty.parentProperty.promoter.id = :promoterId AND c.status <> 'PAID'")
        Double getUnpaidFeeByPromoter(@Param("promoterId") Long promoterId);

        // Cas 2 : Enfants d'une propriété spécifique
        @Query("SELECT SUM(c.fee) FROM CallForCharge c WHERE c.realEstateProperty.parentProperty.id = :propertyId")
        Double getTotalFeeByParentProperty(@Param("propertyId") Long propertyId);

        @Query("SELECT SUM(c.fee) FROM CallForCharge c WHERE c.realEstateProperty.parentProperty.id = :propertyId AND c.status = 'PAID'")
        Double getPaidFeeByParentProperty(@Param("propertyId") Long propertyId);

        @Query("SELECT SUM(c.fee) FROM CallForCharge c WHERE c.realEstateProperty.parentProperty.id = :propertyId AND c.status <> 'PAID'")
        Double getUnpaidFeeByParentProperty(@Param("propertyId") Long propertyId);

        /// coproprietaire
        @Query("SELECT COALESCE(SUM(c.fee), 0) FROM CallForCharge c WHERE c.owner.id = :ownerId AND c.status = 'PAID'")
        double sumPaidByOwner(@Param("ownerId") Long ownerId);

        List<CallForCharge> findByWork(Work work);

        List<CallForCharge> findByRealEstateProperty(RealEstateProperty property);

}
