package com.wakana.vefa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import com.wakana.vefa.model.Meet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;

@Repository
public interface MeetRepository extends JpaRepository<Meet, Long> {
        Page<Meet> findByCreator_Id(Long creatorId, Pageable pageable);

        Page<Meet> findByCreator_IdAndDateTimeBetween(Long creatorId, LocalDateTime startDate, LocalDateTime endDate,
                        Pageable pageable);

        @Query("SELECT m FROM Meet m JOIN m.participants p WHERE p.id = :userId AND m.dateTime BETWEEN :startDate AND :endDate")
        Page<Meet> findByParticipantIdAndDateTimeBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate,
                        Pageable pageable);

        Page<Meet> findByRealEstatePropertyId(Long creatorId, Pageable pageable);

        // Méthode existante : Meetings basés sur le bien immobilier

        // Meetings futurs (à venir) basés sur le bien immobilier
        @Query("SELECT m FROM Meet m WHERE m.realEstateProperty.id = :realEstatePropertyId AND m.dateTime > :currentDateTime ORDER BY m.dateTime ASC")
        Page<Meet> findUpcomingByRealEstatePropertyId(Long realEstatePropertyId, LocalDateTime currentDateTime,
                        Pageable pageable);

        // Meetings passés basés sur le bien immobilier
        @Query("SELECT m FROM Meet m WHERE m.realEstateProperty.id = :realEstatePropertyId AND m.dateTime <= :currentDateTime ORDER BY m.dateTime DESC")
        Page<Meet> findPastByRealEstatePropertyId(Long realEstatePropertyId, LocalDateTime currentDateTime,
                        Pageable pageable);

        long countByRealEstatePropertyIdAndDateTimeAfter(Long propertyId, LocalDateTime now);

        long countByRealEstatePropertyIdAndDateTimeBefore(Long propertyId, LocalDateTime now);
}
