package com.dev.tagashira.repository;

import com.dev.tagashira.entity.UtilityBill;
import com.dev.tagashira.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String>, JpaSpecificationExecutor<Vehicle> {
    List<Vehicle> findAllByApartment_AddressNumber(Long addressNumber);
    Optional<Vehicle> findById(String id);
}
