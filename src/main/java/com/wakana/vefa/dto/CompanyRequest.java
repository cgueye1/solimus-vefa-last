package com.wakana.vefa.dto;
import org.springframework.web.multipart.MultipartFile;

import lombok.Data;
@Data

public class CompanyRequest {


    private Long id;
    private String name;
    private String address;
    private String registrationNumber;
    private String ninea;
    private String primaryColor;
    private String secondaryColor;
    private MultipartFile logo;

    
}
