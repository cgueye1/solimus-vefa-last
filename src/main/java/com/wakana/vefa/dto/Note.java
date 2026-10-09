package com.wakana.vefa.dto;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class Note {
    private MultipartFile file;
    private String subject;
    private String content;
    private Map<String, String> data;
    private String image;
}