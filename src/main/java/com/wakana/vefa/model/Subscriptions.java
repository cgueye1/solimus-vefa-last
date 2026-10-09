package com.wakana.vefa.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.wakana.vefa.enums.PaymentCallStatusEnum;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity

@Table(
   name = "subscriptions2",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "subscription_plans_id"})
    }
)
public class Subscriptions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @ManyToOne
    @JoinColumn(name = "subscription_plans_id")
    private SubscriptionPlans subscriptionPlans;

    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private double paidAmount;
    private int installmentCount;
    private LocalDate dateInvoice;

    @Enumerated(EnumType.STRING)
    private PaymentCallStatusEnum status;

    private boolean renewed = false;
    private int currentProjectCount = 0; // combien de projets actifs liés
    private int remainingProjects; // calculé automatiquement

   // @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
 //   private List<RealEstateProperty> properties = new ArrayList<>();

    public boolean canCreateNewProject() {
        return subscriptionPlans.isUnlimitedProjects()
                || currentProjectCount < subscriptionPlans.getProjectLimit();
    }

    public void incrementProjects() {
        if (!subscriptionPlans.isUnlimitedProjects()) {
            this.currentProjectCount++;
            this.remainingProjects = subscriptionPlans.getProjectLimit() - this.currentProjectCount;
        }
    }

    public void decrementProjects() {
        if (!subscriptionPlans.isUnlimitedProjects() && this.currentProjectCount > 0) {
            this.currentProjectCount--;
            this.remainingProjects = subscriptionPlans.getProjectLimit() - this.currentProjectCount;
        }
    }
}
