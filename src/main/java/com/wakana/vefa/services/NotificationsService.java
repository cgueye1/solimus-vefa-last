package com.wakana.vefa.services;

import com.wakana.vefa.model.Notifications;
import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NotificationsService {

    Notifications sendReservationNotif(String subject,String descri, RealEstateProperty property,User user);
    
    void sendOnlyFCM(String subject,String descri,User user);

    Page<Notifications> getNotificationsByPromoterId(Long promoterId, int page, int size);

    Optional<Notifications> updateNotificationStatus(Long id, boolean status);

    public Page<Notifications> getNotificationsByUserId(Long userId, Pageable pageable);
}
