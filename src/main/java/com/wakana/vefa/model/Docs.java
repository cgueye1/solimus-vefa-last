package com.wakana.vefa.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "documents")
public class Docs {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    
    private String initPdf;
    
    private String signedPdf;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private RealEstateProperty realEstateProperty;
    
    private LocalDateTime lastUpdated;
    
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SignatoryStatus> signatories = new ArrayList<>();
    
    // Helper method to add signatory
    public void addSignatory(User user) {
        SignatoryStatus status = new SignatoryStatus();
        status.setDocument(this);
        status.setSignatory(user);
        status.setHasSigned(false);
        this.signatories.add(status);
    }
    
    // Helper method to remove all signatories
    public void clearSignatories() {
        this.signatories.clear();
    }
}