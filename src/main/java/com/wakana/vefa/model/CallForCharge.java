package com.wakana.vefa.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.wakana.vefa.enums.PaymentCallStatusEnum;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "call_for_charge")
public class CallForCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String label;
    private String file;

    private double fee;
    private String expectedDate;
    @Enumerated(EnumType.STRING)
    private PaymentCallStatusEnum status;
    @ManyToOne
    @JoinColumn(name = "real_estate_property_id", nullable = false)
    private RealEstateProperty realEstateProperty;

    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charge_owner_id", nullable = false)
    private User owner;

    private Long updatedAt;

    private Long createdAt;

    @Transient
    private Date parsedExpectedDate;
    // Nouveau champ
    private Long paymentDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_id")
    private Work work;
}
