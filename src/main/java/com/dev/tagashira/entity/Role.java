package com.dev.tagashira.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false)
    String name; // ROLE_MANAGER, ROLE_ACCOUNTANT

    String description;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    Set<User> users = new HashSet<>();

    @Column(name = "is_active")
    @Builder.Default
    Integer isActive = 1;

    @PrePersist
    protected void onCreate() {
        if (this.isActive == null) {
            this.isActive = 1;
        }
    }
} 