package com.dev.tagashira.controller;

import com.dev.tagashira.constant.VehicleEnum;
import com.dev.tagashira.dto.response.ApiResponse;
import com.dev.tagashira.entity.VehiclePriceSetting;
import com.dev.tagashira.service.VehiclePriceSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicle-price-settings")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class VehiclePriceSettingController {
    
    private final VehiclePriceSettingService vehiclePriceSettingService;
    
    /**
     * Get all active vehicle price settings
     */
    @GetMapping
    public ResponseEntity<List<VehiclePriceSetting>> getAllActiveSettings() {
        List<VehiclePriceSetting> settings = vehiclePriceSettingService.getAllActiveSettings();
        return ResponseEntity.ok(settings);
    }
    
    /**
     * Get price for specific vehicle type
     */
    @GetMapping("/price/{vehicleType}")
    public ResponseEntity<BigDecimal> getPriceForVehicle(@PathVariable VehicleEnum vehicleType) {
        BigDecimal price = vehiclePriceSettingService.getPriceForVehicle(vehicleType);
        return ResponseEntity.ok(price);
    }
    
    /**
     * Create or update vehicle price setting
     */
    @PostMapping
    public ResponseEntity<VehiclePriceSetting> createOrUpdatePriceSetting(
            @RequestParam VehicleEnum vehicleType,
            @RequestParam BigDecimal pricePerVehicle) {
        
        VehiclePriceSetting setting = vehiclePriceSettingService.createOrUpdatePriceSetting(vehicleType, pricePerVehicle);
        return ResponseEntity.ok(setting);
    }
    
    /**
     * Deactivate vehicle price setting
     */
    @DeleteMapping("/{vehicleType}")
    public ResponseEntity<ApiResponse<String>> deactivatePriceSetting(@PathVariable VehicleEnum vehicleType) {
        vehiclePriceSettingService.deactivatePriceSetting(vehicleType);
        
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(HttpStatus.OK.value());
        response.setMessage("Vehicle price setting deactivated successfully");
        response.setData(null);
        
        return ResponseEntity.ok(response);
    }
} 