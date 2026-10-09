package com.wakana.vefa.repository;

import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.enums.RealEstatePropertyStatusEnum;
import com.wakana.vefa.model.RealEstateProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RealEstatePropertyRepository
    extends JpaRepository<RealEstateProperty, Long>, JpaSpecificationExecutor<RealEstateProperty> {

  Page<RealEstateProperty> findByPromoterIdAndParentPropertyIsNullAndRentalFalseAndCoOwnerFalseOrderByNameAsc(
      Long promoterId, Pageable pageable);

  Page<RealEstateProperty> findByBankIdAndParentPropertyIsNullAndRentalFalseAndCoOwnerFalse(Long promoterId,
      Pageable pageable);

  Page<RealEstateProperty> findByAgencyIdAndParentPropertyIsNullAndRentalFalseAndCoOwnerFalse(Long promoterId,
      Pageable pageable);

  Page<RealEstateProperty> findByParentPropertyIdAndRentalFalseAndCoOwnerFalse(Long parentPropertyId,
      Pageable pageable);

  Page<RealEstateProperty> findByParentPropertyIdAndRentalFalseAndCoOwnerFalseAndStatus(
      Long parentPropertyId,
      RealEstatePropertyStatusEnum status,
      Pageable pageable);

  Page<RealEstateProperty> findByRecipientIdAndRentalFalseAndCoOwnerFalse(Long recipientId, Pageable pageable);

  Page<RealEstateProperty> findByRecipientId(Long recipientId, Pageable pageable);

  Page<RealEstateProperty> findByNotaryIdAndParentPropertyIsNotNullAndStatusAndRentalFalseAndCoOwnerFalse(
      Long notaryId, RealEstatePropertyStatusEnum status, Pageable pageable);

  Page<RealEstateProperty> findByPromoterIdAndCoOwnerTrueAndRentalFalse(Long promoterId, Pageable pageable);

  long countByParentPropertyIsNullAndRentalFalseAndCoOwnerFalse();

  long countByPromoterIdAndParentPropertyIsNullAndRentalFalseAndCoOwnerFalse(Long promoterId);

  long countByBankIdAndParentPropertyIsNullAndRentalFalseAndCoOwnerFalse(Long promoterId);

  long countByAgencyIdAndParentPropertyIsNullAndRentalFalseAndCoOwnerFalse(Long promoterId);

  long countByPromoterIdAndParentPropertyIsNotNullAndStatusAndRentalFalseAndCoOwnerFalse(Long promoterId,
      RealEstatePropertyStatusEnum status);

  long countByParentPropertyAgencyIdAndParentPropertyIsNotNullAndStatusAndRentalFalseAndCoOwnerFalse(
      Long promoterId,
      RealEstatePropertyStatusEnum status);

  long countByParentPropertyBankIdAndParentPropertyIsNotNullAndStatusAndRentalFalseAndCoOwnerFalse(
      Long promoterId,
      RealEstatePropertyStatusEnum status);

  /*
   * Page<RealEstateProperty> findByPromoterIdAndParentPropertyIsNull(Long
   * promoterId, Pageable pageable);
   * 
   * Page<RealEstateProperty> findByBankIdAndParentPropertyIsNull(Long promoterId,
   * Pageable pageable);
   * 
   * Page<RealEstateProperty> findByAgencyIdAndParentPropertyIsNull(Long
   * promoterId, Pageable pageable);
   * 
   * Page<RealEstateProperty> findByParentPropertyId(Long parentPropertyId,
   * Pageable pageable);
   * 
   * Page<RealEstateProperty> findByRecipientId(Long recipientId, Pageable
   * pageable);
   * 
   * Page<RealEstateProperty>
   * findByNotaryIdAndParentPropertyIsNotNullAndStatus(Long notaryId,
   * RealEstatePropertyStatusEnum status, Pageable pageable);
   * 
   * Page<RealEstateProperty> findByPromoterIdAndCoOwnerTrue(Long promoterId,
   * Pageable pageable);
   * 
   * long countByParentPropertyIsNull();
   * 
   * long countByPromoterIdAndParentPropertyIsNull(Long promoterId);
   * 
   * long countByBankIdAndParentPropertyIsNull(Long promoterId);
   * 
   * long countByAgencyIdAndParentPropertyIsNull(Long promoterId);
   * 
   * long countByPromoterIdAndParentPropertyIsNotNullAndStatus(Long promoterId,
   * RealEstatePropertyStatusEnum status);
   * 
   * long countByAgencyIdAndParentPropertyIsNotNullAndStatus(Long promoterId,
   * RealEstatePropertyStatusEnum status);
   * 
   * long countByBankIdAndParentPropertyIsNotNullAndStatus(Long promoterId,
   * RealEstatePropertyStatusEnum status);
   */

  // syndic
  @Query("SELECT p FROM RealEstateProperty p JOIN p.owners o WHERE o.id = :userId")
  Set<RealEstateProperty> findPropertiesByOwnerId(Long userId);

  /// location
  Page<RealEstateProperty> findByPromoterIdAndParentPropertyIsNullAndRentalTrueAndCoOwnerFalseOrderByIdDesc(
      Long promoterId,
      Pageable pageable);

  List<RealEstateProperty> findByRentalTrueAndRentalDateNotNullAndRecipientNotNull();

  // dash

  List<RealEstateProperty> findBySoldAtBetweenAndPromoter_Id(LocalDateTime start, LocalDateTime end,
      Long promoterId);

  List<RealEstateProperty> findBySoldAtBetweenAndParentProperty_IdAndPromoter_Id(LocalDateTime start,
      LocalDateTime end,
      Long parentPropertyId,
      Long promoterId);

  List<RealEstateProperty> findBySoldAtBetweenAndParentProperty_IdAndParentProperty_Agency_Id(LocalDateTime start,
      LocalDateTime end,
      Long parentPropertyId,
      Long promoterId);

  List<RealEstateProperty> findBySoldAtBetweenAndParentProperty_IdAndParentProperty_Bank_Id(LocalDateTime start,
      LocalDateTime end,
      Long parentPropertyId,
      Long promoterId);

  long countByParentPropertyId(Long parentPropertyId);

  long countByParentPropertyIdAndStatus(Long parentPropertyId, RealEstatePropertyStatusEnum status);

  ///// for agency and bank
  List<RealEstateProperty> findBySoldAtBetweenAndParentProperty_Agency_Id(LocalDateTime start, LocalDateTime end,
      Long promoterId);

  List<RealEstateProperty> findBySoldAtBetweenAndParentProperty_Bank_Id(LocalDateTime start, LocalDateTime end,
      Long promoterId);

  ///// With sub
  @Query("""
      SELECT r FROM RealEstateProperty r
      JOIN r.subscription s
      JOIN s.subscriptionPlans sp
      WHERE r.promoter.id = :promoterId
      AND r.parentProperty IS NULL
      AND r.rental = false
      AND r.coOwner = false
      AND (:planName IS NULL OR sp.name = :planName)
      ORDER BY r.name ASC
      """)
  Page<RealEstateProperty> searchByPromoterAndParentIsNullWithPlan(
      @Param("promoterId") Long promoterId,
      @Param("planName") ProfilEnum planName,
      Pageable pageable);

  @Query("""
      SELECT r FROM RealEstateProperty r
      JOIN r.subscription s
      JOIN s.subscriptionPlans sp
      WHERE r.promoter.id = :promoterId
      AND r.parentProperty IS NULL
      AND r.rental = true
      AND r.coOwner = false
      AND (:planName IS NULL OR sp.name = :planName)
      ORDER BY r.id DESC
      """)
  Page<RealEstateProperty> searchByPromoterAndRentalWithPlan(
      @Param("promoterId") Long promoterId,
      @Param("planName") ProfilEnum planName,
      Pageable pageable);

  @Query("""
      SELECT r FROM RealEstateProperty r
      JOIN r.subscription s
      JOIN s.subscriptionPlans sp
      WHERE r.agency.id = :agencyId
      AND r.parentProperty IS NULL
      AND r.rental = false
      AND r.coOwner = false
      AND (:planName IS NULL OR sp.name = :planName)
      ORDER BY r.timestamp DESC
      """)
  Page<RealEstateProperty> searchByAgencyAndParentIsNullWithPlan(
      @Param("agencyId") Long agencyId,
      @Param("planName") ProfilEnum planName,
      Pageable pageable);

  @Query("""
          SELECT p FROM RealEstateProperty p
          JOIN p.subscription s
          JOIN s.subscriptionPlans sp
          WHERE p.promoter.id = :promoterId
            AND p.coOwner = true
            AND p.rental = false
            AND (:planName IS NULL OR sp.name = :planName)
          ORDER BY p.id DESC
      """)
  Page<RealEstateProperty> searchByPromoterAndSyndicWithPlan(
      @Param("promoterId") Long promoterId,
      @Param("planName") ProfilEnum planName,
      Pageable pageable);

  /// pend abonnement en charge

  long countByParentProperty_IdAndSubscription_SubscriptionPlans_Name(
      Long parentPropertyId,
      ProfilEnum planName);

  long countByParentProperty_IdAndStatusAndSubscription_SubscriptionPlans_Name(
      Long parentPropertyId,
      RealEstatePropertyStatusEnum status,
      ProfilEnum planName);
  
  
  
   /**
     * Biens principaux (parent = null)
     * Filtrés par promoteur + plan de souscription
     */
    long countByPromoterIdAndParentPropertyIsNullAndRentalFalseAndCoOwnerFalseAndSubscription_SubscriptionPlans_Name(
            Long promoterId,
            ProfilEnum plan
    );

    /**
     * Lots (parent != null)
     * Le plan appartient AU PARENT
     * Filtrés par promoteur du parent + statut
     */
    long countByParentProperty_Promoter_IdAndStatusAndParentProperty_Subscription_SubscriptionPlans_Name(
            Long promoterId,
            RealEstatePropertyStatusEnum status,
            ProfilEnum plan
    );
    
    
    
     List<RealEstateProperty> findByRentalTrueAndRecipientIsNotNull();

}
