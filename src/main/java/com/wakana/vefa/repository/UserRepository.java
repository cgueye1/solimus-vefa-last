package com.wakana.vefa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.model.User;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByTelephone(String telephone);

    Optional<User> findByEmail(String email);

    boolean existsByTelephone(String telephone);

    Page<User> findByNomContainingOrPrenomContainingAndProfilNot(String nom, String prenom, ProfilEnum profil,
            Pageable pageable);

    Page<User> findByProfil(ProfilEnum profil, Pageable pageable);

    Page<User> findByProfilAndNomContainingIgnoreCaseOrProfilAndPrenomContainingIgnoreCase(
            ProfilEnum profil1, String nom,
            ProfilEnum profil2, String prenom,
            Pageable pageable);

    Page<User> findAllByProfilNot(ProfilEnum profil, Pageable pageable);

    List<User> findByProfil(ProfilEnum profil);

    long countByProfil(ProfilEnum profil);

    List<User> findByProfilAndCompany_NameContaining(ProfilEnum profil, String keyword);

    @Query("SELECT u FROM User u JOIN u.properties rp WHERE rp.id = :propertyId")
    Set<User> findOwnersByPropertyId(Long propertyId);

    /// admin
    @Query("SELECT u FROM User u WHERE u.profil = :profil " +
            "AND (:keyword IS NULL OR LOWER(u.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR u.telephone LIKE CONCAT('%', :keyword, '%'))")
    Page<User> findByProfilAndKeyword(
            @Param("profil") ProfilEnum profil,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT COUNT(s) > 0 FROM Subscription s WHERE s.user.id = :userId AND s.active = true")
    boolean hasActiveSubscription(@Param("userId") Long userId);

    boolean existsByEmail(String email);

    Page<User> findByProfilIn(Pageable pageable, List<ProfilEnum> profils);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByProfilAndCreatedAtBetween(
            ProfilEnum profil,
            LocalDateTime start,
            LocalDateTime end);

    @Query("""
                SELECT u.profil, COUNT(u)
                FROM User u
                WHERE u.profil IN :profils
                  AND u.createdAt BETWEEN :start AND :end
                GROUP BY u.profil
            """)
    List<Object[]> countByProfilInAndYear(
            @Param("profils") List<ProfilEnum> profils,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("""
                SELECT MONTH(u.createdAt), COUNT(u)
                FROM User u
                WHERE YEAR(u.createdAt) = :year
                GROUP BY MONTH(u.createdAt)
                ORDER BY MONTH(u.createdAt)
            """)
    List<Object[]> countUsersGroupedByMonth(@Param("year") int year);

    // --------- RÉPARTITION SANS ANNÉE ---------
    @Query("""
                SELECT u.profil, COUNT(u)
                FROM User u
                WHERE u.profil IN :profils
                GROUP BY u.profil
            """)
    List<Object[]> countByProfilIn(@Param("profils") List<ProfilEnum> profils);

    // --------- RÉPARTITION AVEC ANNÉE ---------
    @Query("""
                SELECT u.profil, COUNT(u)
                FROM User u
                WHERE u.profil IN :profils
                AND u.createdAt BETWEEN :start AND :end
                GROUP BY u.profil
            """)
    List<Object[]> countByProfilInAndCreatedAtBetween(
            @Param("profils") List<ProfilEnum> profils,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT u FROM User u WHERE u.email = :username OR u.telephone = :username")
    Optional<User> findByEmailOrTelephone(@Param("username") String username);

}
