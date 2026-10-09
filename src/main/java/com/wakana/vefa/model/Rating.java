package com.wakana.vefa.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "rating")
@Data
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
    @ManyToOne(optional = true)
    @JoinColumn(name = "job_request_id", nullable = true)
    private JobRequest jobRequest;
    @Column(nullable = false)
    private Integer score;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String comment;
}
