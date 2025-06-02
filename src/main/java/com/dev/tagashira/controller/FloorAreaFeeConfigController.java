package com.dev.tagashira.controller;

import com.dev.tagashira.entity.FloorAreaFeeConfig;
import com.dev.tagashira.constant.FeeTypeEnum;
import com.dev.tagashira.service.FloorAreaFeeConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/floor-area-fee-configs")
@CrossOrigin(origins = "http://localhost:5173")
public class FloorAreaFeeConfigController {
    
    private final FloorAreaFeeConfigService configService;
    
    /**
     * Get all active floor area fee configs
     */
    @GetMapping
    public ResponseEntity<List<FloorAreaFeeConfig>> getAllActiveConfigs() {
        List<FloorAreaFeeConfig> configs = configService.getAllActiveConfigs();
        return ResponseEntity.ok(configs);
    }
    
    /**
     * Get configs ready for auto generation
     */
    @GetMapping("/auto-generation")
    public ResponseEntity<List<FloorAreaFeeConfig>> getAutoGenerationConfigs() {
        List<FloorAreaFeeConfig> configs = configService.getAutoGenerationConfigs();
        return ResponseEntity.ok(configs);
    }
    
    /**
     * Get currently effective configs
     */
    @GetMapping("/effective")
    public ResponseEntity<List<FloorAreaFeeConfig>> getCurrentlyEffectiveConfigs() {
        List<FloorAreaFeeConfig> configs = configService.getCurrentlyEffectiveConfigs();
        return ResponseEntity.ok(configs);
    }
    
    /**
     * Get configs by fee type
     */
    @GetMapping("/by-type/{feeType}")
    public ResponseEntity<List<FloorAreaFeeConfig>> getConfigsByFeeType(@PathVariable FeeTypeEnum feeType) {
        List<FloorAreaFeeConfig> configs = configService.getConfigsByFeeType(feeType);
        return ResponseEntity.ok(configs);
    }
    
    /**
     * Get config by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<FloorAreaFeeConfig> getConfigById(@PathVariable Long id) {
        Optional<FloorAreaFeeConfig> config = configService.findById(id);
        return config.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Create new floor area fee config
     */
    @PostMapping
    public ResponseEntity<FloorAreaFeeConfig> createConfig(@RequestBody FloorAreaFeeConfig config) {
        try {
            FloorAreaFeeConfig createdConfig = configService.createConfig(config);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdConfig);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update existing floor area fee config
     */
    @PutMapping("/{id}")
    public ResponseEntity<FloorAreaFeeConfig> updateConfig(
            @PathVariable Long id, 
            @RequestBody FloorAreaFeeConfig config) {
        try {
            FloorAreaFeeConfig updatedConfig = configService.updateConfig(id, config);
            return ResponseEntity.ok(updatedConfig);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Deactivate config (soft delete)
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateConfig(@PathVariable Long id) {
        try {
            configService.deactivateConfig(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Delete config permanently
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConfig(@PathVariable Long id) {
        try {
            configService.deleteConfig(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Initialize default configs (for testing/setup)
     */
    @PostMapping("/initialize-defaults")
    public ResponseEntity<String> initializeDefaultConfigs() {
        configService.initializeDefaultConfigs();
        return ResponseEntity.ok("Default configurations initialized successfully");
    }
} 