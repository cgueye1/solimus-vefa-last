package com.wakana.vefa.dto;
import org.springframework.web.multipart.MultipartFile;

import lombok.Data;


@Data
public class InformationSheetRequest {
 

    private String title;
    private String fileName;
   
     private MultipartFile file;
   
    private Long notaryId; 
}
