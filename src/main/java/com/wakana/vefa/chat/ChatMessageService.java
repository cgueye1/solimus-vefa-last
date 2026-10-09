package com.wakana.vefa.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.wakana.vefa.chatroom.ChatRoom;
import com.wakana.vefa.chatroom.ChatRoomRepository;
import com.wakana.vefa.chatroom.ChatRoomService;
import com.wakana.vefa.model.User;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Date;
@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository repository;
    private final ChatRoomService chatRoomService;
        private final ChatRoomRepository chatRoomRepository;


    public ChatMessage save(ChatMessage chatMessage) {
        
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Africa/Dakar"));
        Date date = Date.from(now.toInstant());
      
        var chatId = chatRoomService
                .getChatRoomId(chatMessage.getSenderId(), chatMessage.getRecipientId(), true)
                .orElseThrow();
                
                 
        chatMessage.setChatId(chatId);
        chatMessage.setTimestamp(date);
        List<ChatRoom> chatRooms=  chatRoomRepository.findByChatId(chatId);
        
        for( ChatRoom chatroom :chatRooms){
            
            chatroom.setTimestamp(date);
            chatroom.setLastMessage(chatMessage.getContent());
            
            if(chatroom.getRecipientId().equals(chatMessage.getRecipientId())){
       
                    chatroom.setUnread(chatroom.getUnread()+1);
                  
                
            }
            chatRoomRepository.save(chatroom);
        }
        return   repository.save(chatMessage);
    }

    public List<ChatMessage> findChatMessages(String senderId, String recipientId) {
        var chatId = chatRoomService.getChatRoomId(senderId, recipientId, false).orElse(null);
        return repository.findByChatId(chatId);
        
        
    }
    
    
    
    void cleanUnread(String chatId,String recipientId ){
                Optional< ChatRoom> optionalChatRoom = chatRoomRepository.findByChatIdAndRecipientId(chatId, recipientId);
                ChatRoom chatRoom = optionalChatRoom.get();
                chatRoom.setUnread(0);
                chatRoomRepository.save(chatRoom);
    }
}
