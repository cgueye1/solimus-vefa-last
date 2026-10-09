package com.wakana.vefa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String logo;
    private String primaryColor;
    private String secondaryColor;
    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;  
    
}

