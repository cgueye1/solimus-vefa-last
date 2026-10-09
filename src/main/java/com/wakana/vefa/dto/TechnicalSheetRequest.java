package com.wakana.vefa.dto;
import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class TechnicalSheetRequest {
    private MultipartFile file;
    private long id;
    private String sheetName;
 
}
