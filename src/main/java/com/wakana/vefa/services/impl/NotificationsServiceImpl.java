package com.wakana.vefa.services.impl;

import com.google.api.client.util.Value;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.wakana.vefa.dto.Note;
import com.wakana.vefa.model.Notifications;
import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.NotificationsRepository;
import com.wakana.vefa.services.FirebaseMessagingService;
import com.wakana.vefa.services.NotificationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class NotificationsServiceImpl implements NotificationsService {
    private final NotificationsRepository notificationsRepository;

    @Value("${app.image.upload-url}")
    private String uploadUrl;
    private final FirebaseMessagingService firebaseService;

    public Page<Notifications> getNotificationsByPromoterId(Long promoterId, int page, int size) {
        return notificationsRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public Optional<Notifications> updateNotificationStatus(Long id, boolean status) {
        Optional<Notifications> notification = notificationsRepository.findById(id);
        if (notification.isPresent()) {
            Notifications updatedNotification = notification.get();
            updatedNotification.setStatus(status);
            notificationsRepository.save(updatedNotification);
            return Optional.of(updatedNotification);
        }
        return Optional.empty();
    }

    public Page<Notifications> getNotificationsByUserId(Long userId, Pageable pageable) {
        return notificationsRepository.findByUserId(userId, pageable);
    }

    @Override
    public Notifications sendReservationNotif(String subject, String descri, RealEstateProperty property, User user) {

        Notifications notifications = new Notifications();
        notifications.setDescription(descri);
        notifications.setProperty(property);
        notifications.setUser(user);
        notifications.setStatus(false);
        notifications.setCreatedAt(LocalDateTime.now());
        /// send push notification
        Note note = new Note();
        note.setSubject(subject);
        note.setContent(descri);
        note.setImage(uploadUrl + property.getPlan());

        try {
            firebaseService.sendNotification(note, "vefa_" + user.getId());
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }

        return notificationsRepository.save(notifications);
    }

    @Override
    public void sendOnlyFCM(String subject, String descri,User user) {

        /*
         * Notifications notifications = new Notifications();
         * notifications.setDescription(descri);
         * notifications.setProperty(property);
         * notifications.setUser(user);
         * notifications.setStatus(false);
         * notifications.setCreatedAt(LocalDateTime.now());
         */

        /// send push notification
        Note note = new Note();
        note.setSubject(subject);
        note.setContent(descri);
       
        try {
            firebaseService.sendNotification(note, "vefa_" + user.getId());
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }

    }
}
