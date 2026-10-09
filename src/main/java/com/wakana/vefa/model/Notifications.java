package com.wakana.vefa.model;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Data
@Entity
@Table(name = "notifications")
public class Notifications {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private RealEstateProperty property; 
    private boolean status;  
    private LocalDateTime createdAt;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;
    
    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;
    
    
  
}
