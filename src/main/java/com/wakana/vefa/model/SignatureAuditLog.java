package com.wakana.vefa.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "signature_audit_log", indexes = {
        @Index(name = "idx_signature_audit_signed_at", columnList = "signed_at"),
        @Index(name = "idx_signature_audit_signer_phone", columnList = "signer_phone"),
        @Index(name = "idx_signature_audit_project", columnList = "project_id")
})
public class SignatureAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Docs document;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "signatory_id", nullable = false)
    private User signatory;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private RealEstateProperty property;

    @Column(name = "document_title")
    private String documentTitle;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "signer_name")
    private String signerName;

    @Column(name = "signer_phone")
    private String signerPhone;

    @Column(name = "client_ip_address", nullable = false)
    private String clientIpAddress;

    @Column(name = "signed_at", nullable = false)
    private LocalDateTime signedAt;

    @Column(length = 500)
    private String signatureNotes;

    @PrePersist
    void onCreate() {
        if (signedAt == null) {
            signedAt = LocalDateTime.now();
        }
    }
}