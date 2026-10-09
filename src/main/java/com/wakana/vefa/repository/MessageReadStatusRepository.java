package com.wakana.vefa.repository;

import com.wakana.vefa.model.MessageReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MessageReadStatusRepository extends JpaRepository<MessageReadStatus, Long> {

    // Nombre de messages non lus pour un user dans un bien
    @Query("""
                SELECT COUNT(m) FROM Message m
                WHERE m.property.id = :propertyId
                AND m.sender.id != :userId
                AND NOT EXISTS (
                    SELECT r FROM MessageReadStatus r
                    WHERE r.message = m AND r.user.id = :userId
                )
            """)
    long countUnread(@Param("propertyId") Long propertyId, @Param("userId") Long userId);

    // Vérifie si un message est déjà lu par un user
    Optional<MessageReadStatus> findByMessageIdAndUserId(Long messageId, Long userId);
}