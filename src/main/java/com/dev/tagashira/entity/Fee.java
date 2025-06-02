package com.dev.tagashira.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.dev.tagashira.constant.FeeTypeEnum;
import com.dev.tagashira.service.SecurityUtil;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static jakarta.persistence.FetchType.LAZY;

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
    FeeTypeEnum feeTypeEnum; // Unified fee type enum

    @Column(nullable = false)
    BigDecimal amount; // Final calculated amount to be paid (VNĐ)

    @Column(nullable = true)
    BigDecimal unitPrice; // Unit price for reference (VNĐ per unit - xe, m², etc.)

    @Column(nullable = true)
    Long apartmentId; // Apartment this fee applies to (for specific apartment fees)

    // Phí định kỳ
    @Builder.Default
    @Column(nullable = false)
    Boolean isRecurring = false; // true cho Monthly Fee, false cho Fee thường
    
    @Builder.Default
    @Column(nullable = false)
    Boolean isActive = true; // Trạng thái kích hoạt
    
    // Thời gian hiệu lực
    LocalDate effectiveFrom;
    LocalDate effectiveTo;

    LocalDate createdAt;
    LocalDate updatedAt;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = LocalDate.now();
    }
    
    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = LocalDate.now();
    }
    
    // ============ HELPER METHODS ============
    
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
     * Check if this fee is voluntary (can be applied to any apartment)
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
}
