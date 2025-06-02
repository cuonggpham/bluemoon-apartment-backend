package com.dev.tagashira.repository;

import com.dev.tagashira.constant.VehicleEnum;
import com.dev.tagashira.entity.VehiclePriceSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiclePriceSettingRepository extends JpaRepository<VehiclePriceSetting, Long> {
    
    Optional<VehiclePriceSetting> findByVehicleTypeAndIsActiveTrue(VehicleEnum vehicleType);
    
    List<VehiclePriceSetting> findByIsActiveTrueOrderByVehicleTypeAsc();
    
    boolean existsByVehicleTypeAndIsActiveTrue(VehicleEnum vehicleType);
} 