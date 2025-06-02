package com.dev.tagashira.entity;

import com.dev.tagashira.constant.VehicleEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "vehicle_price_settings")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehiclePriceSetting {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    VehicleEnum vehicleType;
    
    @Column(nullable = false)
    BigDecimal pricePerVehicle; // Price per vehicle per month (VNĐ)
    
    @Column(nullable = false)
    Boolean isActive = true;
    
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
} 