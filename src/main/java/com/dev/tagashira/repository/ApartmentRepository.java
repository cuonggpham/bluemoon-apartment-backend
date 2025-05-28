package com.dev.tagashira.repository;

import com.dev.tagashira.entity.Apartment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, Long>, JpaSpecificationExecutor<Apartment> {
    @EntityGraph(attributePaths = {"residentList", "owner"})
    Optional<Apartment> findById(Long addressNumber);
    
    Optional<Apartment> findByOwnerId(Long ownerId);
    
    // Find all apartments owned by a specific resident
    List<Apartment> findAllByOwnerId(Long ownerId);
}
