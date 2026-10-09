package com.wakana.vefa.services.V2;

import org.springframework.data.domain.Page;
import com.wakana.vefa.dto.v2.MessageDTO;
import com.wakana.vefa.dto.v2.SendMessageRequest;

public interface MessageService {
    Page<MessageDTO> getMessages(Long propertyId, int page, int size);

    MessageDTO sendMessage(Long propertyId, SendMessageRequest request, String username);

    long getUnreadCount(Long propertyId, String username); // nouveau

    void markAsRead(Long messageId, String username); // nouveau
}