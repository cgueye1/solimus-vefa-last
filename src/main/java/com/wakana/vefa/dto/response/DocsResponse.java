package com.wakana.vefa.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DocsResponse {
    private Long id;
    private String title;
    private String initPdf;
    private String signedPdf;
    private Long propertyId;
    private LocalDateTime lastUpdated;
    private List<SignatoryInfo> signatories;
    
    @Data
    public static class SignatoryInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private boolean hasSigned;
        private LocalDateTime signedAt;
        private String signatureNotes;
        
        public SignatoryInfo(Long id, String firstName, String lastName, 
                            boolean hasSigned, LocalDateTime signedAt, String signatureNotes) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.hasSigned = hasSigned;
            this.signedAt = signedAt;
            this.signatureNotes = signatureNotes;
        }
    }
}