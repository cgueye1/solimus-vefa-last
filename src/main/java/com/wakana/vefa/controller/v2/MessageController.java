package com.wakana.vefa.controller.v2;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.wakana.vefa.dto.v2.MessageDTO;
import com.wakana.vefa.dto.v2.SendMessageRequest;
import com.wakana.vefa.services.V2.MessageService;

@RestController
@RequestMapping("/api/properties-v2")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // GET /api/properties/{propertyId}/chat?page=0&size=20
    @GetMapping("/{propertyId}/chat")
    public ResponseEntity<Page<MessageDTO>> getMessages(
            @PathVariable Long propertyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(messageService.getMessages(propertyId, page, size));
    }

    // POST /api/properties/{propertyId}/chat
    @PostMapping("/{propertyId}/chat")
    public ResponseEntity<MessageDTO> sendMessage(
            @PathVariable Long propertyId,
            @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                messageService.sendMessage(propertyId, request, userDetails.getUsername()));
    }

    // GET /api/properties/{propertyId}/chat/unread
    @GetMapping("/{propertyId}//unread")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @PathVariable Long propertyId,
            @AuthenticationPrincipal UserDetails userDetails) {

        long count = messageService.getUnreadCount(propertyId, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("unread", count));
    }

    // POST /api/properties/{propertyId}/chat/{messageId}/read
    @PostMapping("/{propertyId}/{messageId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long propertyId,
            @PathVariable Long messageId,
            @AuthenticationPrincipal UserDetails userDetails) {

        messageService.markAsRead(messageId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}