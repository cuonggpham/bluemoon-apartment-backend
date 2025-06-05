package com.dev.tagashira.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id", nullable = false)
    @JsonIgnore
    Resident payer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_id", nullable = false)
    @JsonIgnore
    Fee fee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    @JsonIgnore
    Apartment apartment;

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

    @Transient
    Long payerId;
    
    @Transient
    Long feeId;
    
    @Transient
    Long apartmentId;

    @PostLoad
    public void onLoad() {
        this.payerId = payer != null ? payer.getId() : null;
        this.feeId = fee != null ? fee.getId() : null;
        this.apartmentId = apartment != null ? apartment.getAddressNumber() : null;
    }
} 