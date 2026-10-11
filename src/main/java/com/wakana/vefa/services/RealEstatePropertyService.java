package com.wakana.vefa.services;

import com.wakana.vefa.dto.DashboardStats;
import com.wakana.vefa.dto.RealEstatePropertyRequest;
import com.wakana.vefa.dto.RealEstatePropertyResponse;
import com.wakana.vefa.dto.RealEstatePropertySearchRequest;
import com.wakana.vefa.dto.ReservationStats;
import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.enums.RealEstatePropertyStatusEnum;
import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.model.User;

import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RealEstatePropertyService {

        /**
         * -----------------------
         * CRUD Property
         * -----------------------
         */

        @CacheEvict(value = { "properties", "dashboard", "lists" }, allEntries = true)
        RealEstateProperty save(RealEstatePropertyRequest request, String planFileName, String legalStatusFileName,
                        List<String> pictureUrls);

        @CacheEvict(value = { "properties", "dashboard", "lists" }, allEntries = true)
        RealEstateProperty update(Long id, RealEstatePropertyRequest request, String planFileName,
                        String legalStatusFileName, List<String> pictureUrls);

        @CacheEvict(value = { "properties", "dashboard", "lists" }, allEntries = true)
        void delete(Long id);

        /**
         * -----------------------
         * Search & Lists
         * -----------------------
         */

        @Cacheable(value = "lists", key = "{'lotProperties', #request, #pageable}")
        Page<RealEstateProperty> searchLotPropertiesByParent(RealEstatePropertySearchRequest request,
                        Pageable pageable);

        // @Cacheable(value = "lists", key = "{'promoterRoot', #promoterId, #pageable}")
        // Page<RealEstatePropertyResponse> searchByPromoterAndParentIsNull(Long
        // promoterId, Pageable pageable);

        @Cacheable(value = "lists", key = "{'bankRoot', #id, #pageable}")
        Page<RealEstatePropertyResponse> searchByBankAndParentIsNull(Long id, Pageable pageable);

        // @Cacheable(value = "lists", key = "{'agencyRoot', #id, #pageable}")
        // Page<RealEstatePropertyResponse> searchByAgencyAndParentIsNull(Long id,
        // Pageable pageable);

        @Cacheable(value = "lists", key = "{'parent', #parentPropertyId, #pageable}")
        Page<RealEstateProperty> findByParentPropertyId(Long parentPropertyId, Pageable pageable);

        @Cacheable(value = "lists", key = "{'availableParent', #parentPropertyId, #pageable}")
        Page<RealEstateProperty> findAvailableByParentPropertyId(Long parentPropertyId, Pageable pageable);

        @Cacheable(value = "lists", key = "{'search', #request, #pageable}")
        Page<RealEstateProperty> searchProperties(RealEstatePropertySearchRequest request, Pageable pageable);

        @Cacheable(value = "lists", key = "{'searchRental', #request, #pageable}")
        Page<RealEstateProperty> searchPropertiesRental(RealEstatePropertySearchRequest request, Pageable pageable);

        /**
         * -----------------------
         * Operations on Property
         * -----------------------
         */

        @CacheEvict(value = { "properties", "dashboard", "lists" }, allEntries = true)
        RealEstateProperty changeStatus(Long propertyId, RealEstatePropertyStatusEnum status);

        @CacheEvict(value = { "properties", "dashboard", "lists" }, allEntries = true)
        RealEstateProperty changeRecipient(Long propertyId, Long userId);

        @Cacheable(value = "properties", key = "#propertyId")
        RealEstatePropertyResponse getProperty(Long propertyId);

        @Cacheable(value = "lists", key = "{'recipient', #recipientId, #pageable}")
        Page<RealEstateProperty> getPropertiesByRecipientId(Long recipientId, Pageable pageable);

        @Cacheable(value = "lists", key = "{'recipientAll', #recipientId, #pageable}")
        Page<RealEstateProperty> getPropertiesByRecipientIdAll(Long recipientId, Pageable pageable);

        @Cacheable(value = "lists", key = "{'reservedByNotary', #notaryId, #name, #status, #pageable}")
        Page<RealEstateProperty> getReservedPropertiesByNotaryId(Long notaryId, String name,
                        RealEstatePropertyStatusEnum status, Pageable pageable);

        @CacheEvict(value = { "properties", "dashboard", "lists" }, allEntries = true)
        RealEstatePropertyResponse validateSale(Long propertyId, User notary);

        @Cacheable(value = "dashboard", key = "'noParent'")
        long countPropertiesWithoutParent();

        /**
         * -----------------------
         * Dashboard & Stats
         * -----------------------
         */

        @Cacheable(value = "dashboard", key = "#p0")
        DashboardStats getDashboardStats(Long promoterId,String name);

        @CacheEvict(value = "dashboard", allEntries = true)
        RealEstatePropertyResponse payLotFee(Long propertyId);

        /**
         * -----------------------
         * Copropriété
         * -----------------------
         */

        @CacheEvict(value = { "properties", "dashboard" }, allEntries = true)
        RealEstatePropertyResponse addOwner(Long propertyId, Long userId, double area);

        @Cacheable(value = "lists", key = "{'coOwner', #promoterId, #pageable}")

        Page<RealEstatePropertyResponse> searchByPromoterAndSyndic(
                        Long promoterId,
                        ProfilEnum planName,
                        Pageable pageable);
        /*
         * Page<RealEstatePropertyResponse> findByPromoterIdAndCoOwnerTrue(Long
         * promoterId, Pageable pageable);
         */

        @Cacheable(value = "lists", key = "{'byOwner', #userId}")
        Set<RealEstateProperty> findPropertiesByOwnerId(Long userId);

        @CacheEvict(value = { "properties", "dashboard" }, allEntries = true)
        RealEstatePropertyResponse addBudget(Long id, double budget);

        @CacheEvict(value = { "properties", "dashboard" }, allEntries = true)
        void allocateBudgetAndGenerateCalls(Long propertyId);

        /**
         * -----------------------
         * Rentals
         * -----------------------
         */

        // @Cacheable(value = "lists", key = "{'rentalByPromoter', #promoterId,
        // #pageable}")
        // Page<RealEstatePropertyResponse> searchByPromoterAndRental(Long promoterId,
        // Pageable pageable);

        /**
         * -----------------------
         * Reporting
         * -----------------------
         */

        @Cacheable(value = "stats", key = "{#year, #promoterId}")
        List<ReservationStats> getSalesByMonth(int year, Long promoterId);

        @Cacheable(value = "stats", key = "{#year, #parentPropertyId, #promoterId}")
        List<ReservationStats> getSalesByMonthByPropertyId(int year, Long parentPropertyId, Long promoterId);

        @Cacheable(value = "dashboard", key = "'lots_' + #parentPropertyId")
        DashboardStats getLotsDashboardStats(Long parentPropertyId, String name);

        /**
         * -----------------------
         * Reservations
         * -----------------------
         */

        @CacheEvict(value = { "properties", "dashboard", "lists" }, allEntries = true)
        void cancelRes(Long id);

        Page<RealEstatePropertyResponse> searchByPromoterAndParentIsNull(
                        Long promoterId,
                        ProfilEnum planName,
                        Pageable pageable);

        Page<RealEstatePropertyResponse> searchByPromoterAndRental(
                        Long promoterId,
                        ProfilEnum planName,
                        Pageable pageable);

        Page<RealEstatePropertyResponse> searchByAgencyAndParentIsNull(
                        Long agencyId,
                        ProfilEnum planName,
                        Pageable pageable);
        
        
        
        RealEstateProperty addSub(Long id, String profil);
        
        
        RealEstatePropertyResponse removeOwner(Long propertyId, Long userId);
}
