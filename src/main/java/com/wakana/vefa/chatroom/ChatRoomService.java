
package com.wakana.vefa.chatroom;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.UserRepository;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
        private final UserRepository userRepository;


    public Optional<String> getChatRoomId(String senderId, String recipientId, boolean createNewRoomIfNotExists) {
        
        
      return   chatRoomRepository
                .findBySenderIdAndRecipientId(senderId, recipientId)
                .map(ChatRoom::getChatId)
                .or(() -> {
                    if (createNewRoomIfNotExists) {
                        return Optional.of(createChatId(senderId, recipientId));
                    }
                    return Optional.empty();
                });
    }

    private String createChatId(String senderId, String recipientId) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Africa/Dakar"));
        Date date = Date.from(now.toInstant());
        String chatId = String.format("%s_%s", senderId, recipientId);

        if (chatRoomRepository.existsBySenderIdAndRecipientId(senderId, recipientId) ||
            chatRoomRepository.existsBySenderIdAndRecipientId(recipientId, senderId)) {
                
            return chatId;
        }
        
        Optional<User> optionalSender =  findRecipientOrSendById(senderId);
    
        
        Optional<User> optionalRecipient =  findRecipientOrSendById(recipientId);
     

        ChatRoom senderRecipient = new ChatRoom();
        senderRecipient.setChatId(chatId);
        senderRecipient.setSenderId(senderId);
        senderRecipient.setRecipientId(recipientId);
        optionalSender.ifPresent(  senderRecipient::setSender);
        optionalRecipient.ifPresent(  senderRecipient::setRecipient);
        senderRecipient.setTimestamp(date);
        

        ChatRoom recipientSender = new ChatRoom();
        recipientSender.setChatId(chatId);
        recipientSender.setSenderId(recipientId);
        recipientSender.setRecipientId(senderId);
        optionalRecipient.ifPresent( recipientSender::setSender);
        optionalSender.ifPresent( recipientSender::setRecipient);
        recipientSender.setTimestamp(date);
        
        

        chatRoomRepository.save(senderRecipient);
        chatRoomRepository.save(recipientSender);

        return chatId;
    }

    public List<ChatRoom> getChatRoomsForUser(String userId) {
        return chatRoomRepository.findAllBySenderId(userId);
    }
    
    
    

 
 
    public  Page<ChatRoom> findByRecipientIdAndSenderNameContaining(String id, String keyword,
    Pageable pageable) {
return chatRoomRepository.findByRecipientIdAndSenderNameContaining(id, keyword, pageable);
}



public Page<ChatRoom> findAllByRecipientId(String id, 
Pageable pageable) {
  return chatRoomRepository.findAllByRecipientIdOrderByTimestampDesc(id,pageable);
}
 
 
 
public Optional<User>  findRecipientOrSendById(String recipientId) {
    try {
        Long id = Long.parseLong(recipientId);
        return userRepository.findById(id);
    } catch (NumberFormatException e) {
        // Gérer le cas où recipientId n'est pas un nombre valide
        System.err.println("Invalid recipientId: " + recipientId);
        return Optional.empty();  // ou lever une exception personnalisée si nécessaire
    }
}
}

