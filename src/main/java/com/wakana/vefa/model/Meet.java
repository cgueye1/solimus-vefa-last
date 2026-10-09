package com.wakana.vefa.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "meet_class")
public class Meet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime dateTime; // Use LocalDateTime for storing date and time
    private int duration; // Duration in minutes
    @ManyToMany
    private List<User> participants; // List of User entities
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = true)
    private User creator;
    private String title;
    private String address;
    @ElementCollection
    @Column(name = "meet_files")
    private List<String> files;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "real_estate_property_id", nullable = false)
    private RealEstateProperty realEstateProperty;

    private Boolean isOnline;

}
