package com.dev.tagashira.repository;

import com.dev.tagashira.entity.FloorAreaFeeConfig;
import com.dev.tagashira.constant.FeeTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FloorAreaFeeConfigRepository extends JpaRepository<FloorAreaFeeConfig, Long> {
    
    /**
     * Find all active floor area fee configs
     */
    List<FloorAreaFeeConfig> findByIsActiveTrueOrderByFeeNameAsc();
    
    /**
     * Find all configs that are ready for auto generation
     */
    List<FloorAreaFeeConfig> findByIsActiveTrueAndIsAutoGeneratedTrueOrderByScheduledDayAscScheduledHourAscScheduledMinuteAsc();
    
    /**
     * Find configs by fee type
     */
    List<FloorAreaFeeConfig> findByFeeTypeEnumAndIsActiveTrueOrderByFeeNameAsc(FeeTypeEnum feeTypeEnum);
    
    /**
     * Find config by fee name (unique per active config)
     */
    Optional<FloorAreaFeeConfig> findByFeeNameAndIsActiveTrue(String feeName);
    
    /**
     * Check if config exists by fee name and is active
     */
    boolean existsByFeeNameAndIsActiveTrue(String feeName);
    
    /**
     * Find all currently effective configs (within date range)
     */
    @Query("SELECT f FROM FloorAreaFeeConfig f WHERE f.isActive = true " +
           "AND (f.effectiveFrom IS NULL OR f.effectiveFrom <= :currentDate) " +
           "AND (f.effectiveTo IS NULL OR f.effectiveTo >= :currentDate) " +
           "ORDER BY f.feeName ASC")
    List<FloorAreaFeeConfig> findCurrentlyEffectiveConfigs(@Param("currentDate") LocalDate currentDate);
    
    /**
     * Find configs scheduled for specific day/hour/minute
     */
    @Query("SELECT f FROM FloorAreaFeeConfig f WHERE f.isActive = true AND f.isAutoGenerated = true " +
           "AND f.scheduledDay = :day AND f.scheduledHour = :hour AND f.scheduledMinute = :minute")
    List<FloorAreaFeeConfig> findBySchedule(@Param("day") Integer day, 
                                           @Param("hour") Integer hour, 
                                           @Param("minute") Integer minute);
} 