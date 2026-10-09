package com.wakana.vefa.model;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "invoice")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String invoiceNumber;

    private double amount;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription2_id")
    private Subscriptions subscription;

    private boolean paid;

    private String paymentMethod; // e.g., "CARD", "BANK_TRANSFER"

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
