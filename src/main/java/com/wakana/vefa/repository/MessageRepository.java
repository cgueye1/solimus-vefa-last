package com.wakana.vefa.repository;



import com.wakana.vefa.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByPropertyIdOrderBySentAtDesc(Long propertyId, Pageable pageable);
}