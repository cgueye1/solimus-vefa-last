package com.wakana.vefa.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RentalContractResponse {

    private Long id;
    private Long propertyId;
    private String propertyName;

    private Long recipientId;
    private String recipientName;

    private LocalDate startDate;
    private LocalDate endDate;

    private boolean active;
    private boolean archived;

    private String contractFile;
    private LocalDateTime createdAt;
}
