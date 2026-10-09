package com.wakana.vefa.chatroom;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

public interface ChatRoomRepository  extends JpaRepository< ChatRoom , Long> {
    Optional<ChatRoom> findBySenderIdAndRecipientId(String senderId, String recipientId);
    List<ChatRoom> findAllBySenderId(String senderId);
    boolean existsBySenderIdAndRecipientId(String senderId, String recipientId);
    
    Page<ChatRoom> findAllByRecipientIdOrderByTimestampDesc(String recipientId, Pageable pageable);
    
      @Query("SELECT c FROM ChatRoom c " +
           "JOIN c.sender s " +
           "WHERE c.recipientId = :recipientId " +
           "AND (LOWER(s.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.prenom) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY c.timestamp DESC")
    Page<ChatRoom> findByRecipientIdAndSenderNameContaining(
        @Param("recipientId") String recipientId,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    List<ChatRoom> findByChatId(String chatId);
    Optional<ChatRoom> findByChatIdAndRecipientId(String chatId, String recipientId);

    

    
}
