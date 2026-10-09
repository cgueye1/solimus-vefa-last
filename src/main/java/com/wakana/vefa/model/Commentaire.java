package com.wakana.vefa.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Data
@Entity
@Table(name = "commentaire")
public class Commentaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private  RealEstateProperty property;
    
     @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Commentaire parent;


    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Commentaire> reponses= new ArrayList<>(); // Initialisation avec une liste vide

    private  String auteur;
    private String auteurimg; 
    
    private String contenu;
    private String date;
    private Long idauteur;
    
    // Constructeurs, getters et setters
}
