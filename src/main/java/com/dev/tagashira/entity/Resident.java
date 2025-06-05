package com.dev.tagashira.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.dev.tagashira.constant.GenderEnum;
import com.dev.tagashira.constant.ResidentEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

@Entity
@Table(name = "residentList")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Resident {
    @Id
    Long id;

    String name;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    LocalDate dob;

    GenderEnum gender;

    String cic;    
    
    @ManyToMany(mappedBy = "residentList", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    Set<Apartment> apartments = new HashSet<>();

    @OneToMany(mappedBy = "payer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    List<PaymentRecord> paymentRecords;

    @Enumerated(EnumType.STRING)
    ResidentEnum status;
    int isActive;
    LocalDate statusDate;

    @Transient
    ResidentEnum previousStatus;

    @Transient
    Long apartmentId;

    @PrePersist
    public void beforePersist() {
        isActive = 1;
        statusDate = LocalDate.now();
    }    @PostLoad
    public void onLoad() {
        this.previousStatus = this.status;
        // For backward compatibility, get first apartment if exists
        this.apartmentId = apartments != null && !apartments.isEmpty() 
            ? apartments.iterator().next().getAddressNumber() 
            : null;
    }

    @PreUpdate
    public void beforeUpdate() {
        if (!status.equals(previousStatus)) {
            this.statusDate = LocalDate.now();
        }
        this.previousStatus = this.status;
        if (this.isActive == 0) {
            this.status = ResidentEnum.Moved;
        }
    }
}