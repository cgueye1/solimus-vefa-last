package com.wakana.vefa.model;

import java.util.List;
import com.wakana.vefa.enums.RequestStateEnum;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "job_requests")
public class JobRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private SolimusJobs job;
    @Column(nullable = false)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User requester;

    @ManyToOne(optional = true)
    @JoinColumn(name = "service_provider_id", nullable = true)
    private User serviceProvider;

    @ManyToOne(optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private RealEstateProperty property;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStateEnum state;

    @ElementCollection
    @Column(name = "pictures")
    private List<String> pictures;

    private String devis;

}
