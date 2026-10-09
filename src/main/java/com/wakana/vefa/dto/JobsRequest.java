package com.wakana.vefa.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class JobsRequest {
    private MultipartFile file;
    private String name;
    private String icon;

}
