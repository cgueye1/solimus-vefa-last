package com.wakana.vefa.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "information_sheets")
public class InformationSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String file;
    @ManyToOne
    @JoinColumn(name = "notary_id", nullable = false)
    private User notary; 
}
