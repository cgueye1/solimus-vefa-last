package com.wakana.vefa.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class MeetFileRequest {
    private MultipartFile file;
    private Long meetId;
    private String label;
    private String fileName;

}
