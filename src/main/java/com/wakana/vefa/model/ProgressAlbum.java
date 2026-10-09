package com.wakana.vefa.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "progress_album")
public class ProgressAlbum { 

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "real_estate_property_id", nullable = false)
    @JsonIgnore
    private RealEstateProperty realEstateProperty;
    private String phaseName;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;
    private LocalDateTime lastUpdated;
    @ElementCollection
    @Column(name = "pictures")
    private List<String> pictures;
    private boolean entrance;
    private boolean archived;

    /*
     * @PrePersist
     * 
     * @PreUpdate
     * public void setLastUpdated() {
     * this.lastUpdated = LocalDateTime.now();
     * }
     */
    public ProgressAlbum() {
    }

    public ProgressAlbum(String phaseName, RealEstateProperty realEstateProperty, boolean entrance,
            List<String> pictures) {
        this.phaseName = phaseName;
        this.realEstateProperty = realEstateProperty;
        this.pictures = pictures;
        this.entrance = entrance;
        this.lastUpdated = LocalDateTime.now();
    }
}
