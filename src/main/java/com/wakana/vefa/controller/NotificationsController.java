package com.wakana.vefa.controller;

import com.wakana.vefa.model.Notifications;
import com.wakana.vefa.services.NotificationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationsController {

    private final NotificationsService notificationsService;

    @GetMapping("/promoter/{promoterId}")
    public ResponseEntity<Page<Notifications>> getNotificationsByPromoterId(
            @PathVariable Long promoterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(notificationsService.getNotificationsByPromoterId(promoterId, page, size));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Notifications> updateNotificationStatus(
            @PathVariable Long id,
            @RequestParam boolean status) {
        Optional<Notifications> updatedNotification = notificationsService.updateNotificationStatus(id, status);
        return updatedNotification.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public Page<Notifications> getNotificationsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return notificationsService.getNotificationsByUserId(userId, PageRequest.of(page, size));
    }
}
