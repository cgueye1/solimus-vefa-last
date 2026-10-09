package com.wakana.vefa.dto.v2;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private Long id;
    private String content;
    private LocalDateTime sentAt;

    // Sender info
    private Long senderId;
    private String senderNom;
    private String senderPrenom;
    private String senderPhoto;
    private String senderProfil;   // ProfilEnum.name()
}