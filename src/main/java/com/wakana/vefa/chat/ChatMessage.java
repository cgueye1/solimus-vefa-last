package com.wakana.vefa.chat;
import lombok.Data;
import jakarta.persistence.*;

import java.util.Date;

@Data
@Entity
@Table(name = "chatmessage")
public class ChatMessage {
   @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;
    private String chatId;
    private String senderId;
    private String recipientId;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;
    private Date timestamp;
    
    
    public enum MessageType {
        CHAT, JOIN, LEAVE
    }
}
