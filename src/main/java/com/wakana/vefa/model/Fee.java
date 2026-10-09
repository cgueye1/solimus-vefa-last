package com.wakana.vefa.model;
import com.wakana.vefa.enums.FeeEnum;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "fee", uniqueConstraints = @UniqueConstraint(columnNames = "name")) 
public class Fee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private FeeEnum name; 
    private double fee;
}
