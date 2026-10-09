package com.wakana.vefa.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ProgressAlbumRequest {
    private Long realEstatePropertyId;
    private String phaseName;
    private String description;
    private List<MultipartFile> pictures;
   private  boolean entrance;
}
