package com.wakana.vefa.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SignatureRequest {
    private Long documentId;
    private Long userId;
    private MultipartFile signedPdf;
    private String signatureNotes;
}