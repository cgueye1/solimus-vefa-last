package com.wakana.vefa.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.wakana.vefa.enums.PaymentCallStatusEnum;

@Data
@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = true)
    private String reference;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @ManyToOne
    @JoinColumn(name = "subscription_plan_id", nullable = false)
    private SubscriptionPlan subscriptionPlan;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private double paidAmount;
    private int installmentCount;
    private LocalDate dateInvoice;
    @Enumerated(EnumType.STRING)
    private PaymentCallStatusEnum status;
    private boolean renewed = false;

}
