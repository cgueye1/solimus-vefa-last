package com.wakana.vefa.services.impl.v2;

import com.wakana.vefa.dto.v2.MessageDTO;
import com.wakana.vefa.dto.v2.SendMessageRequest;
import com.wakana.vefa.model.Message;
import com.wakana.vefa.model.MessageReadStatus;
import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.MessageReadStatusRepository;
import com.wakana.vefa.repository.MessageRepository;
import com.wakana.vefa.repository.RealEstatePropertyRepository;
import com.wakana.vefa.repository.UserRepository;
import com.wakana.vefa.services.V2.MessageService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final RealEstatePropertyRepository propertyRepository;
    private final UserRepository userRepository;
    // Ajouter le repo dans le constructeur
    private final MessageReadStatusRepository readStatusRepository;

    @Override
    public Page<MessageDTO> getMessages(Long propertyId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return messageRepository
                .findByPropertyIdOrderBySentAtDesc(propertyId, pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional
    public MessageDTO sendMessage(Long propertyId, SendMessageRequest request, String username) {
        RealEstateProperty property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Bien introuvable : " + propertyId));

        User sender = userRepository.findByEmailOrTelephone(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + username));

        Message message = new Message();
        message.setContent(request.getContent());
        message.setProperty(property);
        message.setSender(sender);
        message.setSentAt(LocalDateTime.now());

        return toDTO(messageRepository.save(message));
    }

    private MessageDTO toDTO(Message m) {
        MessageDTO dto = new MessageDTO();
        dto.setId(m.getId());
        dto.setContent(m.getContent());
        dto.setSentAt(m.getSentAt());
        dto.setSenderId(m.getSender().getId());
        dto.setSenderNom(m.getSender().getNom());
        dto.setSenderPrenom(m.getSender().getPrenom());
        dto.setSenderPhoto(m.getSender().getPhoto());
        dto.setSenderProfil(m.getSender().getProfil() != null
                ? m.getSender().getProfil().name()
                : null);
        return dto;
    }

    @Override
    public long getUnreadCount(Long propertyId, String username) {
        User user = userRepository.findByEmailOrTelephone(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        return readStatusRepository.countUnread(propertyId, user.getId());
    }

    @Override
    @Transactional
    public void markAsRead(Long messageId, String username) {
        User user = userRepository.findByEmailOrTelephone(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Évite les doublons
        if (readStatusRepository.findByMessageIdAndUserId(messageId, user.getId()).isPresent()) {
            return;
        }

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message introuvable"));

        // Ne pas marquer ses propres messages
        if (message.getSender().getId().equals(user.getId()))
            return;

        MessageReadStatus status = new MessageReadStatus();
        status.setMessage(message);
        status.setUser(user);
        status.setReadAt(LocalDateTime.now());
        readStatusRepository.save(status);
    }
}