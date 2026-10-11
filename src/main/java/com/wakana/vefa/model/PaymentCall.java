package com.wakana.vefa.model;

import com.wakana.vefa.enums.PaymentCallStatusEnum;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "payment_calls")
public class PaymentCall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label;
    private String ref;
    private String file;

    private double percentage;
    private double amount;
    private double baseAmount;

    private String expectedDate;

    @Enumerated(EnumType.STRING)
    private PaymentCallStatusEnum status;

    @ManyToOne
    @JoinColumn(name = "real_estate_property_id", nullable = false)
    private RealEstateProperty realEstateProperty;
    private Long updatedAt;

    private Long createdAt;

    @Transient
    private Date parsedExpectedDate;

    @Transient
    private boolean overdue;

    private double tom;
    private double tva;
}
