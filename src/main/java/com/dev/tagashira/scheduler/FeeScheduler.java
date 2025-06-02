package com.dev.tagashira.scheduler;

import com.dev.tagashira.constant.FeeTypeEnum;
import com.dev.tagashira.entity.FloorAreaFeeConfig;
import com.dev.tagashira.service.FeeService;
import com.dev.tagashira.service.FloorAreaFeeConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeeScheduler {
    
    private final FeeService feeService;
    private final FloorAreaFeeConfigService floorAreaFeeConfigService;
    
    /**
     * Initialize default configs on application startup
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeDefaultConfigs() {
        floorAreaFeeConfigService.initializeDefaultConfigs();
    }
    
    /**
     * Tự động tạo phí gửi xe hàng tháng
     * Chạy vào 1:00 AM ngày 1 hàng tháng
     */
    @Scheduled(cron = "0 0 1 1 * ?")
    public void generateMonthlyVehicleParkingFees() {
        try {
            LocalDate now = LocalDate.now();
            String billingMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            
            log.info("Starting automatic generation of vehicle parking fees for month: {}", billingMonth);
            
            var generatedFees = feeService.generateMonthlyFeesForAllApartments(
                FeeTypeEnum.VEHICLE_PARKING, 
                billingMonth, 
                null,
                null
            );
            
            log.info("Successfully generated {} vehicle parking fees for month: {}", 
                generatedFees.size(), billingMonth);
                
        } catch (Exception e) {
            log.error("Failed to generate monthly vehicle parking fees", e);
        }
    }
    
    /**
     * Tự động tạo phí theo diện tích sàn dựa trên cấu hình
     * Chạy mỗi 15 phút để kiểm tra các cấu hình scheduled
     */
    @Scheduled(cron = "0 */15 * * * ?") // Every 15 minutes
    public void generateScheduledFloorAreaFees() {
        try {
            LocalDate today = LocalDate.now();
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            int currentDay = today.getDayOfMonth();
            int currentHour = now.getHour();
            int currentMinute = now.getMinute();
            
            // Round down to nearest 15-minute interval
            int roundedMinute = (currentMinute / 15) * 15;
            
            List<FloorAreaFeeConfig> scheduledConfigs = floorAreaFeeConfigService
                .getConfigsScheduledAt(currentDay, currentHour, roundedMinute);
            
            if (!scheduledConfigs.isEmpty()) {
                String billingMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                log.info("Found {} scheduled floor area fee configs for {}:{}:{} on day {}", 
                    scheduledConfigs.size(), currentHour, roundedMinute, 0, currentDay);
                
                for (FloorAreaFeeConfig config : scheduledConfigs) {
                    if (config.isReadyForAutoGeneration()) {
                        generateFloorAreaFeesFromConfig(config, billingMonth);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to generate scheduled floor area fees", e);
        }
    }
    
    /**
     * Generate floor area fees from specific config
     */
    private void generateFloorAreaFeesFromConfig(FloorAreaFeeConfig config, String billingMonth) {
        try {
            log.info("Generating {} for month {} with unit price {} VNĐ/m²", 
                config.getFeeName(), billingMonth, config.getUnitPricePerSqm());
            
            var generatedFees = feeService.generateMonthlyFeesForAllApartments(
                config.getFeeTypeEnum(),
                billingMonth, 
                config.getUnitPricePerSqm(),
                config.getFeeName()
            );
            
            log.info("Successfully generated {} {} fees for month: {}", 
                generatedFees.size(), config.getFeeName(), billingMonth);
                
        } catch (Exception e) {
            log.error("Failed to generate {} for month {}: {}", 
                config.getFeeName(), billingMonth, e.getMessage());
        }
    }
    
    /**
     * Manual generation of all active floor area fees (for testing)
     * Runs at 3:00 AM on the 1st of each month
     */
    @Scheduled(cron = "0 0 3 1 * ?")
    public void generateAllActiveFloorAreaFees() {
        try {
            LocalDate now = LocalDate.now();
            String billingMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            
            log.info("Starting manual generation of all active floor area fees for month: {}", billingMonth);
            
            List<FloorAreaFeeConfig> activeConfigs = floorAreaFeeConfigService.getCurrentlyEffectiveConfigs();
            
            for (FloorAreaFeeConfig config : activeConfigs) {
                // Only generate if not set for auto generation (to avoid duplicates)
                if (!config.getIsAutoGenerated()) {
                    generateFloorAreaFeesFromConfig(config, billingMonth);
                }
            }
            
            log.info("Completed manual generation for {} non-auto configs", 
                activeConfigs.stream().filter(c -> !c.getIsAutoGenerated()).count());
                
        } catch (Exception e) {
            log.error("Failed to generate manual floor area fees", e);
        }
    }
    
    /**
     * Health check scheduler - kiểm tra hệ thống hàng ngày
     * Chạy vào 8:00 AM hàng ngày
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void dailyHealthCheck() {
        try {
            LocalDate now = LocalDate.now();
            String currentMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            
            log.info("Daily health check - Current month: {}", currentMonth);
            
            // Kiểm tra xem đã tạo phí cho tháng hiện tại chưa
            var vehicleFees = feeService.getMonthlyFeesByMonth(currentMonth);
            
            if (vehicleFees.isEmpty() && now.getDayOfMonth() > 1) {
                log.warn("No monthly fees found for current month: {}. Manual intervention may be required.", currentMonth);
            } else {
                log.info("Found {} monthly fees for current month: {}", vehicleFees.size(), currentMonth);
            }
            
            // Check floor area fee configs
            List<FloorAreaFeeConfig> activeConfigs = floorAreaFeeConfigService.getAllActiveConfigs();
            List<FloorAreaFeeConfig> autoConfigs = floorAreaFeeConfigService.getAutoGenerationConfigs();
            
            log.info("Floor area fee configs - Active: {}, Auto-generation: {}", 
                activeConfigs.size(), autoConfigs.size());
            
        } catch (Exception e) {
            log.error("Daily health check failed", e);
        }
    }
} 