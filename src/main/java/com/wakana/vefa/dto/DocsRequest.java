package com.wakana.vefa.dto;


import lombok.Data;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

@Data
public class DocsRequest {
    private String titre;
    private Long propertyId;
    private MultipartFile initPdf;
     private List<Long> signatoryIds; // IDs of users who need to sign
}