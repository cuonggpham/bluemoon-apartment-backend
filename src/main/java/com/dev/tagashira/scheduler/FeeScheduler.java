package com.dev.tagashira.scheduler;

import com.dev.tagashira.constant.FeeTypeEnum;
import com.dev.tagashira.service.FeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeeScheduler {
    
    private final FeeService feeService;
    
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
                null // Vehicle parking doesn't need unit price per sqm
            );
            
            log.info("Successfully generated {} vehicle parking fees for month: {}", 
                generatedFees.size(), billingMonth);
                
        } catch (Exception e) {
            log.error("Failed to generate monthly vehicle parking fees", e);
        }
    }
    
    /**
     * Tự động tạo phí diện tích sàn hàng tháng
     * Chạy vào 1:30 AM ngày 1 hàng tháng
     */
    @Scheduled(cron = "0 30 1 1 * ?")
    public void generateMonthlyFloorAreaFees() {
        try {
            LocalDate now = LocalDate.now();
            String billingMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            
            log.info("Starting automatic generation of floor area fees for month: {}", billingMonth);
            
            // Default unit price: 15,000 VNĐ/m²
            BigDecimal unitPricePerSqm = new BigDecimal("15000");
            
            var generatedFees = feeService.generateMonthlyFeesForAllApartments(
                FeeTypeEnum.FLOOR_AREA, 
                billingMonth, 
                unitPricePerSqm
            );
            
            log.info("Successfully generated {} floor area fees for month: {} with unit price: {} VNĐ/m²", 
                generatedFees.size(), billingMonth, unitPricePerSqm);
                
        } catch (Exception e) {
            log.error("Failed to generate monthly floor area fees", e);
        }
    }
    
    /**
     * Tự động tạo phí quản lý hàng tháng
     * Chạy vào 2:00 AM ngày 1 hàng tháng
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    public void generateMonthlyManagementFees() {
        try {
            LocalDate now = LocalDate.now();
            String billingMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            
            log.info("Starting automatic generation of management fees for month: {}", billingMonth);
            
            // Default unit price: 10,000 VNĐ/m²
            BigDecimal unitPricePerSqm = new BigDecimal("10000");
            
            var generatedFees = feeService.generateMonthlyFeesForAllApartments(
                FeeTypeEnum.MANAGEMENT_FEE, 
                billingMonth, 
                unitPricePerSqm
            );
            
            log.info("Successfully generated {} management fees for month: {} with unit price: {} VNĐ/m²", 
                generatedFees.size(), billingMonth, unitPricePerSqm);
                
        } catch (Exception e) {
            log.error("Failed to generate monthly management fees", e);
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
            
        } catch (Exception e) {
            log.error("Daily health check failed", e);
        }
    }
} 