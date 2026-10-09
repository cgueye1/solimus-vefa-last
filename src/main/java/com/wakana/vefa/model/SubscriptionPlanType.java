package com.wakana.vefa.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "subscription_plan_type")
public class SubscriptionPlanType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String label; // BASIC, PREMIUM, PRO, etc.
    private Long level;

}
