package com.dev.tagashira.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.dev.tagashira.constant.VehicleEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Vehicle {
    @Id
    String id;

    @Enumerated(EnumType.STRING)
    VehicleEnum category;

    @ManyToOne()
    @JoinColumn(name = "address_id")
    @JsonIgnore
    Apartment apartment;

    LocalDate registerDate;

    @PrePersist
    public void beforeCreate() {
        this.registerDate = LocalDate.now();
    }

    @Transient
    Long apartmentId;

    @PostLoad
    public void onLoad() {
        this.apartmentId = apartment != null ? apartment.getAddressNumber() : null;
    }

}
