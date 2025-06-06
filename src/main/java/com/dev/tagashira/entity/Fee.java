package com.dev.tagashira.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.dev.tagashira.constant.FeeTypeEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "fees")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Fee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;    
    
    @Column(nullable = false)
    String name;

    @Column(columnDefinition = "MEDIUMTEXT")
    String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    FeeTypeEnum feeTypeEnum;

    @Column(nullable = false)
    BigDecimal amount;

    @Column()
    BigDecimal unitPrice;

    @ManyToOne
    @JoinColumn(name = "apartment_id", nullable = false)
    @JsonIgnore
    Apartment apartment;

    @OneToOne(mappedBy = "fee", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    PaymentRecord paymentRecord;

    @Builder.Default
    @Column(nullable = false)
    Boolean isRecurring = false;
    
    @Builder.Default
    @Column(nullable = false)
    Boolean isActive = true; // Trạng thái kích hoạt
    
    // Thời gian hiệu lực
    LocalDate effectiveFrom;
    LocalDate effectiveTo;

    LocalDate createdAt;
    LocalDate updatedAt;

    @Transient
    Long apartmentId;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = LocalDate.now();
    }
    
    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = LocalDate.now();
    }
    
    @PostLoad
    public void onLoad() {
        this.apartmentId = apartment != null ? apartment.getAddressNumber() : null;
    }
    
    // ============ HELPER METHODS ============
    
    /**
     * Check if this fee has been paid
     */
    @JsonIgnore
    public boolean isPaid() {
        return paymentRecord != null;
    }
    
    /**
     * Check if this fee is mandatory (requires specific apartment)
     */
    @JsonIgnore
    public boolean isMandatory() {
        Set<FeeTypeEnum> mandatoryTypes = Set.of(
            FeeTypeEnum.MANDATORY,
            FeeTypeEnum.VEHICLE_PARKING,
            FeeTypeEnum.FLOOR_AREA
        );
        return mandatoryTypes.contains(this.feeTypeEnum);
    }
    
    /**
     * Check if this fee is voluntary
     */
    @JsonIgnore
    public boolean isVoluntary() {
        return this.feeTypeEnum == FeeTypeEnum.VOLUNTARY;
    }
    
    /**
     * Check if this fee is calculated per unit (requires unitPrice)
     */
    @JsonIgnore
    public boolean isPerUnitFee() {
        Set<FeeTypeEnum> perUnitTypes = Set.of(
            FeeTypeEnum.VEHICLE_PARKING,  // per vehicle
            FeeTypeEnum.FLOOR_AREA        // per m²
        );
        return perUnitTypes.contains(this.feeTypeEnum);
    }
    
    /**
     * Check if this fee is a monthly recurring fee
     */
    @JsonIgnore
    public boolean isMonthlyFee() {
        Set<FeeTypeEnum> monthlyTypes = Set.of(
            FeeTypeEnum.VEHICLE_PARKING,
            FeeTypeEnum.FLOOR_AREA
        );
        return monthlyTypes.contains(this.feeTypeEnum);
    }
    
    /**
     * Get unit description for this fee type
     */
    @JsonIgnore
    public String getUnitDescription() {
        return switch (this.feeTypeEnum) {
            case VEHICLE_PARKING -> "xe";
            case FLOOR_AREA -> "m²";
            default -> "đơn vị";
        };
    }
    
    @JsonIgnore
    public Long getApartmentNumber() {
        return apartment != null ? apartment.getAddressNumber() : null;
    }
    
    @JsonIgnore
    public String getApartmentInfo() {
        return apartment != null ? "Apartment #" + apartment.getAddressNumber() : "No apartment";
    }
}
