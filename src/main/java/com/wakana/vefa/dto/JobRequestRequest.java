package com.wakana.vefa.dto;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class JobRequestRequest {


    private Long jobId;
    private String description;
    private Long requesterId;
    private Long serviceProviderId;
    private Long propertyId;
    private List<MultipartFile> pictures;

}
