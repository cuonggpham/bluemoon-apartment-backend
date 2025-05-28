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

@Entity
@Table(name = "residents")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_number")
    @JsonIgnore
    Apartment apartment;

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
    }

    @PostLoad
    public void onLoad() {
        this.previousStatus = this.status;
        this.apartmentId = apartment != null ? apartment.getAddressNumber() : null;
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