package com.wakana.vefa.model;

import com.wakana.vefa.enums.SubscriptionPlanEnum;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "subscription_plan", uniqueConstraints = @UniqueConstraint(columnNames = "name")) // Unicité du nom
public class SubscriptionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private SubscriptionPlanEnum name; // Nom unique basé sur l'énumération

    private double totalCost;
    private int installmentCount;  
}
