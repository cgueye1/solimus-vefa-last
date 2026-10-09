package com.wakana.vefa.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class PaymentCallRequest {

    private String label;
    private String ref;

    private double percentage;

    private double amount;

    private String expectedDate;

    private String status;

    private long realEstatePropertyId;

    private MultipartFile file;
    private String fileName;

    private double tom;
    private double tva;

}
