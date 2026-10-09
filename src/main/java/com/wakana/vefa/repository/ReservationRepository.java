package com.wakana.vefa.repository;

import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.model.Reservation;
import com.wakana.vefa.model.User;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.*;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

        // Existing method to find all reservations ordered by createdAt
        List<Reservation> findAllByOrderByCreatedAtDesc();

        Optional<Reservation> findByPropertyAndUser(RealEstateProperty property, User user);

        @Modifying
        @Transactional
        @Query("DELETE FROM Reservation r WHERE r.property = :property")
        void deleteByProperty(@Param("property") RealEstateProperty property);

        @Modifying
        @Transactional
        @Query("DELETE FROM Reservation r WHERE r.property.id = :propertyId")
        void deleteByPropertyId(Long propertyId);

        // Existing method to find reservations by parent property with pagination
        Page<Reservation> findByPropertyParentPropertyIdOrderByCreatedAtDesc(Long parentPropertyId, Pageable pageable);

        Page<Reservation> findByPropertyIdOrderByCreatedAtDesc(Long propertyId, Pageable pageable);

        // New method to find reservations by reservataire (user) ID with pagination
        Page<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

        boolean existsByUserAndProperty(User user, RealEstateProperty property);

        /// promoter dash
        List<Reservation> findByValidatedAtBetweenAndProperty_Promoter_Id(
                        LocalDateTime startDate,
                        LocalDateTime endDate,
                        Long promoterId);

        List<Reservation> findByValidatedAtBetweenAndProperty_ParentProperty_Agency_Id(
                        LocalDateTime startDate,
                        LocalDateTime endDate,
                        Long promoterId);

        List<Reservation> findByValidatedAtBetweenAndProperty_ParentProperty_Bank_Id(
                        LocalDateTime startDate,
                        LocalDateTime endDate,
                        Long promoterId);

        List<Reservation> findByValidatedAtBetweenAndProperty_ParentProperty_IdAndProperty_Promoter_Id(
                        LocalDateTime startDate,
                        LocalDateTime endDate,
                        Long parentPropertyId,
                        Long promoterId);

        List<Reservation> findByValidatedAtBetweenAndProperty_ParentProperty_IdAndProperty_ParentProperty_Agency_Id(
                        LocalDateTime startDate,
                        LocalDateTime endDate,
                        Long parentPropertyId,
                        Long promoterId);

        List<Reservation> findByValidatedAtBetweenAndProperty_ParentProperty_IdAndProperty_ParentProperty_Bank_Id(
                        LocalDateTime startDate,
                        LocalDateTime endDate,
                        Long parentPropertyId,
                        Long promoterId);

        ///
        List<Reservation> findByPropertyAndIdNot(RealEstateProperty property, Long id);

        List<Reservation> findByProperty(RealEstateProperty property);

        // 🔹 Reservations dont le bien est NON locatif
        Page<Reservation> findByUserIdAndProperty_RentalFalseOrderByCreatedAtDesc(
                        Long userId,
                        Pageable pageable);

        // 🔹 Reservations dont le bien est locatif
        Page<Reservation> findByUserIdAndProperty_RentalTrueOrderByCreatedAtDesc(
                        Long userId,
                        Pageable pageable);

        /// sub pris en compte
        List<Reservation> findByValidatedAtBetweenAndProperty_ParentProperty_IdAndProperty_Promoter_IdAndProperty_Subscription_SubscriptionPlans_Name(
                        LocalDateTime start,
                        LocalDateTime end,
                        Long parentPropertyId,
                        Long promoterId,
                        ProfilEnum name);

        List<Reservation> findByValidatedAtBetweenAndProperty_ParentProperty_IdAndProperty_ParentProperty_Agency_IdAndProperty_Subscription_SubscriptionPlans_Name(
                        LocalDateTime start,
                        LocalDateTime end,
                        Long parentPropertyId,
                        Long agencyId,
                        ProfilEnum name);

        List<Reservation> findByValidatedAtBetweenAndProperty_ParentProperty_IdAndProperty_ParentProperty_Bank_IdAndProperty_Subscription_SubscriptionPlans_Name(
                        LocalDateTime start,
                        LocalDateTime end,
                        Long parentPropertyId,
                        Long bankId,
                        ProfilEnum name);

        List<Reservation> findByValidatedAtBetweenAndProperty_Promoter_IdAndProperty_Subscription_SubscriptionPlans_Name(
                        LocalDateTime start,
                        LocalDateTime end,
                        Long promoterId,
                        ProfilEnum name);

        List<Reservation> findByValidatedAtBetweenAndProperty_ParentProperty_Agency_IdAndProperty_Subscription_SubscriptionPlans_Name(
                        LocalDateTime start,
                        LocalDateTime end,
                        Long agencyId,
                        ProfilEnum name);

        List<Reservation> findByValidatedAtBetweenAndProperty_ParentProperty_Bank_IdAndProperty_Subscription_SubscriptionPlans_Name(
                        LocalDateTime start,
                        LocalDateTime end,
                        Long bankId,
                        ProfilEnum name);

}
