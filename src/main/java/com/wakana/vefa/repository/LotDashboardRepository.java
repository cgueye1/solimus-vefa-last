package com.wakana.vefa.repository;

import com.wakana.vefa.enums.RealEstatePropertyStatusEnum;
import com.wakana.vefa.model.RealEstateProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LotDashboardRepository extends JpaRepository<RealEstateProperty, Long> {

    // Tous les lots d'un projet
    @Query("SELECT r FROM RealEstateProperty r WHERE r.parentProperty.id = :projectId")
    List<RealEstateProperty> findLotsByProjectId(@Param("projectId") Long projectId);

    // Lots par statut
    @Query("SELECT r FROM RealEstateProperty r WHERE r.parentProperty.id = :projectId AND r.status = :status")
    List<RealEstateProperty> findLotsByProjectIdAndStatus(@Param("projectId") Long projectId,
            @Param("status") RealEstatePropertyStatusEnum status);

    // Compter lots par statut
    @Query("SELECT COUNT(r) FROM RealEstateProperty r WHERE r.parentProperty.id = :projectId AND r.status = :status")
    long countByProjectIdAndStatus(@Param("projectId") Long projectId,
            @Param("status") RealEstatePropertyStatusEnum status);

    // Lots vendus avec leur date de vente pour vitesse de vente
    @Query("SELECT r FROM RealEstateProperty r WHERE r.parentProperty.id = :projectId AND r.status = 'SOLD' AND r.soldAt IS NOT NULL")
    List<RealEstateProperty> findSoldLotsWithDate(@Param("projectId") Long projectId);
}