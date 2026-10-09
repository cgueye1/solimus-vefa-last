package com.wakana.vefa.dto;


import lombok.Data;
import org.springframework.web.multipart.MultipartFile;


@Data
public class RentalContractRequest {

    private Long propertyId;
    private Long recipientId;

    // format attendu : dd-MM-yyyy
    private String startDate;
    private String endDate;

    private MultipartFile contractFile;
}
