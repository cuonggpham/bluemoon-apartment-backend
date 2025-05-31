package com.dev.tagashira.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

@Entity
@Table(name = "payment_record")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Long payerId; // Foreign key to Resident

    @Column(nullable = false)
    Long feeId; // Foreign key to Fee

    @Column(nullable = false)
    Long apartmentId; // Foreign key to Apartment

    @Column(nullable = false)
    LocalDate paymentDate;

    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal amount;

    @Column(columnDefinition = "MEDIUMTEXT")
    String notes;

    Instant createdAt;
    Instant updatedAt;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
    }
} 