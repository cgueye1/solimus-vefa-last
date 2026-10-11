package com.wakana.vefa.dto.response;

import com.wakana.vefa.model.SignatureAuditLog;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SignatureAuditLogResponse {
    private final Long id;
    private final Long documentId;
    private final Long projectId;
    private final String documentTitle;
    private final String projectName;
    private final Long signerId;
    private final String signerName;
    private final String signerPhone;
    private final String clientIpAddress;
    private final LocalDateTime signedAt;
    private final String signatureNotes;

    public SignatureAuditLogResponse(SignatureAuditLog auditLog) {
        this.id = auditLog.getId();
        this.documentId = auditLog.getDocument().getId();
        this.projectId = auditLog.getProperty().getId();
        this.documentTitle = auditLog.getDocumentTitle();
        this.projectName = auditLog.getProjectName();
        this.signerId = auditLog.getSignatory().getId();
        this.signerName = auditLog.getSignerName();
        this.signerPhone = auditLog.getSignerPhone();
        this.clientIpAddress = auditLog.getClientIpAddress();
        this.signedAt = auditLog.getSignedAt();
        this.signatureNotes = auditLog.getSignatureNotes();
    }
}