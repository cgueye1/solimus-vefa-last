package com.wakana.vefa.services;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.wakana.vefa.dto.Note;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FirebaseMessagingService {
  private final FirebaseMessaging firebaseMessaging;

  public String sendNotification(Note note, String token) throws FirebaseMessagingException {
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Africa/Dakar"));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    String formattedDateTime = now.format(formatter);
    Notification notification = Notification
        .builder()
        .setTitle(note.getSubject())
        .setImage(note.getImage())
        .setBody(note.getContent())
        .build();

    Message message = Message
        .builder()
        // .setToken(token)
        .setTopic(token)
        .setNotification(notification)
        // .putAllData(note.getData())
        .build();

    return firebaseMessaging.send(message);
  }

}