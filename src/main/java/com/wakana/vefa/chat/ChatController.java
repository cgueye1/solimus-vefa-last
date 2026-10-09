package com.wakana.vefa.chat;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wakana.vefa.chatroom.ChatRoom;
import com.wakana.vefa.chatroom.ChatRoomService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatMessageService;
     private final ChatRoomService chatRoomService;


    
     @PostMapping("/messages/send")
    public ResponseEntity<?>   saveFeedback(@RequestBody ChatMessage chatMessage) {
        return ResponseEntity.ok(chatMessageService.save(chatMessage));
        
    }

    @GetMapping("/messages/{senderId}/{recipientId}")
    public ResponseEntity<List<ChatMessage>> findChatMessages(@PathVariable String senderId,
                                      @PathVariable String recipientId) {
        return ResponseEntity
                .ok(chatMessageService.findChatMessages(senderId, recipientId));
    }
    
     @GetMapping("/chats/{userId}")
    public ResponseEntity<List<ChatRoom>>getChatRoomsForUser(@PathVariable String userId) {
        return ResponseEntity
        .ok(chatRoomService.getChatRoomsForUser(userId));
    }
    
    @GetMapping("/chats/clear/{chatId}/{recipientId}")
    public ResponseEntity<?> clearUread(@PathVariable String chatId,@PathVariable String recipientId) {
        chatMessageService.cleanUnread(chatId, recipientId);
        
        return ResponseEntity
        .ok("Clear");
    }
    
    
         
    @GetMapping("/messages/search")
    public ResponseEntity<Page<ChatRoom>> search(
        @RequestParam(required = true) String id,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ChatRoom> chatRoomPage;

        if (keyword == null || keyword.trim().isEmpty()) {
            chatRoomPage = chatRoomService.findAllByRecipientId(id,pageable);
        } else {
            chatRoomPage = chatRoomService.findByRecipientIdAndSenderNameContaining(id,keyword,pageable);        }
        
        long totalElements = chatRoomPage.getTotalElements();
        int totalPages = chatRoomPage.getTotalPages();
        
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("X-Total-Elements", String.valueOf(totalElements));
        responseHeaders.add("X-Total-Pages", String.valueOf(totalPages));
        
        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(chatRoomPage);
    }
    
    
    ////web socket

    /* 
        @MessageMapping("/chat.sendMessage")
    public void sendMessage(ChatMessage chatMessage) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Africa/Dakar"));
        chatMessage.setTimestamp(Date.from(now.toInstant()));

        // Persist message in the database
        chatMessageService.save(chatMessage);

        // Send message to the recipient based on the chatId (private channel)
        String destination = String.format("/topic/chat/%s", chatMessage.getChatId());
        messagingTemplate.convertAndSend(destination, chatMessage);
    }*/
    
    
    
     

    
    
    
}
