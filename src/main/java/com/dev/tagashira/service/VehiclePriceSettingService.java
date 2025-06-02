package com.dev.tagashira.service;

import com.dev.tagashira.constant.VehicleEnum;
import com.dev.tagashira.entity.VehiclePriceSetting;
import com.dev.tagashira.repository.VehiclePriceSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehiclePriceSettingService {
    
    private final VehiclePriceSettingRepository vehiclePriceSettingRepository;
    
    /**
     * Get all active vehicle price settings
     */
    public List<VehiclePriceSetting> getAllActiveSettings() {
        return vehiclePriceSettingRepository.findByIsActiveTrueOrderByVehicleTypeAsc();
    }
    
    /**
     * Get price for specific vehicle type
     */
    public BigDecimal getPriceForVehicle(VehicleEnum vehicleType) {
        return vehiclePriceSettingRepository.findByVehicleTypeAndIsActiveTrue(vehicleType)
            .map(VehiclePriceSetting::getPricePerVehicle)
            .orElseThrow(() -> new RuntimeException("No active price setting found for vehicle type: " + vehicleType));
    }
    
    /**
     * Create or update vehicle price setting
     */
    public VehiclePriceSetting createOrUpdatePriceSetting(VehicleEnum vehicleType, BigDecimal pricePerVehicle) {
        VehiclePriceSetting existing = vehiclePriceSettingRepository
            .findByVehicleTypeAndIsActiveTrue(vehicleType)
            .orElse(null);
            
        if (existing != null) {
            // Update existing
            existing.setPricePerVehicle(pricePerVehicle);
            existing.setUpdatedAt(LocalDate.now());
            return vehiclePriceSettingRepository.save(existing);
        } else {
            // Create new
            VehiclePriceSetting newSetting = VehiclePriceSetting.builder()
                .vehicleType(vehicleType)
                .pricePerVehicle(pricePerVehicle)
                .isActive(true)
                .createdAt(LocalDate.now())
                .build();
            return vehiclePriceSettingRepository.save(newSetting);
        }
    }
    
    /**
     * Deactivate vehicle price setting
     */
    public void deactivatePriceSetting(VehicleEnum vehicleType) {
        vehiclePriceSettingRepository.findByVehicleTypeAndIsActiveTrue(vehicleType)
            .ifPresent(setting -> {
                setting.setIsActive(false);
                setting.setUpdatedAt(LocalDate.now());
                vehiclePriceSettingRepository.save(setting);
            });
    }
} 