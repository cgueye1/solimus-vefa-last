
package com.wakana.vefa.chatroom;
import lombok.Data;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.wakana.vefa.model.User;

import jakarta.persistence.*;

@Data
@Entity
@Table(name = "chatroom")
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chatId;
    private String senderId;
    private String recipientId;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String lastMessage; 
    private int unread; 
    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_user_id")
    private User sender;
    
    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id")
    private User recipient;
    
    private Date timestamp;
}
