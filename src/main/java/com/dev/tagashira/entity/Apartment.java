package com.dev.tagashira.entity;

import com.dev.tagashira.constant.ApartmentEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults; 

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "apartments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
/**
 * Entity class representing an apartment.
 * This class is mapped to the "apartments" table in the database.
 */
public class Apartment {
    @Id
    Long addressNumber; // Unique identifier for the apartment (address number)

    @OneToMany(mappedBy = "apartment")
     List<Resident> residentList;
 
     @OneToOne
     @JoinColumn(name = "owner_id", referencedColumnName = "id")  //The name of the foreign key column in the apartments table refers to the id in the residents table.
     Resident owner;
 
     double area;
     @Enumerated(EnumType.STRING)
     ApartmentEnum status;
 
     Instant createdAt;
     Instant updatedAt;
 
     @PrePersist
     public void beforeCreate() {
         this.createdAt = Instant.now();
     }
 
     @PreUpdate
     public void beforeUpdate() {
         this.updatedAt = Instant.now();
     }
}
