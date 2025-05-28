package com.dev.tagashira.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.dev.tagashira.constant.ApartmentEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.HashSet;
import java.util.Set;

import com.dev.tagashira.constant.VehicleEnum;

@Entity
@Table(name = "apartments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Apartment {
    @Id
    @Column(unique = true, nullable = false)
    Long addressNumber;

    double area;

    @Enumerated(EnumType.STRING)
    ApartmentEnum status;

    Instant createdAt;
    Instant updatedAt;    

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "apartment_residents",
        joinColumns = @JoinColumn(name = "apartment_id"),
        inverseJoinColumns = @JoinColumn(name = "resident_id")
    )
    @Builder.Default
    private Set<Resident> residentList = new HashSet<>();

    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL)
    private List<Vehicle> vehicleList;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    Resident owner;

    Long ownerPhone;

    @JsonIgnore
    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL)
    List<InvoiceApartment> invoiceApartments;

    @Transient
    Integer numberOfMembers;

    @Transient
    @JsonProperty
    Long numberOfMotorbikes;

    @Transient
    @JsonProperty
    Long numberOfCars;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
    }

    @PostLoad
    public void onLoad() {
        numberOfMembers = (int) residentList.stream()
                .filter(resident -> resident.getStatus() != null && resident.getStatus() != com.dev.tagashira.constant.ResidentEnum.Moved)
                .count();
        vehicleList = Optional.ofNullable(vehicleList).orElse(Collections.emptyList());
        numberOfMotorbikes = vehicleList.stream()
                .filter(vehicle -> vehicle.getCategory() == VehicleEnum.Motorbike)
                .count();
        numberOfCars = vehicleList.stream()
                .filter(vehicle -> vehicle.getCategory() == VehicleEnum.Car)
                .count();
    }
}