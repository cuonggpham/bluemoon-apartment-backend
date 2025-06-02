package com.dev.tagashira.repository;

import com.dev.tagashira.entity.UtilityBill;
import com.dev.tagashira.entity.Vehicle;
import com.dev.tagashira.constant.VehicleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String>, JpaSpecificationExecutor<Vehicle> {
    List<Vehicle> findAllByApartment_AddressNumber(Long addressNumber);
    Optional<Vehicle> findById(String id);
    
    /**
     * Count vehicles by apartment and vehicle type for fee calculation
     */
    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.apartment.addressNumber = :apartmentId AND v.category = :vehicleType")
    Long countByApartmentAndVehicleType(@Param("apartmentId") Long apartmentId, @Param("vehicleType") VehicleEnum vehicleType);
    
    /**
     * Get all vehicles for an apartment grouped by type for fee calculation
     */
    @Query("SELECT v.category, COUNT(v) FROM Vehicle v WHERE v.apartment.addressNumber = :apartmentId GROUP BY v.category")
    List<Object[]> countVehiclesByTypeForApartment(@Param("apartmentId") Long apartmentId);
}
