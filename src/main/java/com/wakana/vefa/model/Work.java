package com.wakana.vefa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.wakana.vefa.enums.WorkStatusEnum;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "works")
public class Work {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    private double price;

    private LocalDate startDate;

    private LocalDate deliveryDate;

    @Enumerated(EnumType.STRING)
    private WorkStatusEnum status = WorkStatusEnum.PENDING;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "real_estate_property_id", nullable = false)
    private RealEstateProperty realEstateProperty;
    
    @JsonIgnore
    @OneToMany(mappedBy = "work", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<CallForCharge> calls;

    // 📸 Liste d’images (URLs)
    @ElementCollection
    @CollectionTable(name = "work_pictures", joinColumns = @JoinColumn(name = "work_id"))
    @Column(name = "picture_url")
    private List<String> pictures = new ArrayList<>();
}
