package com.dev.tagashira.repository;

import com.dev.tagashira.constant.FeeTypeEnum;
import com.dev.tagashira.entity.Fee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeeRepository extends JpaRepository<Fee, Long>, JpaSpecificationExecutor<Fee> {
    /**
     * Tìm phí theo tên và apartment ID (để kiểm tra duplicate)
     */
    Optional<Fee> findByNameAndApartmentId(String name, Long apartmentId);
    
    /**
     * Lấy tất cả phí chưa thanh toán của một apartment
     */
    List<Fee> findByApartmentIdAndIsRecurringTrueAndIsActiveTrueOrderByCreatedAtDesc(Long apartmentId);
    
    /**
     * Lấy phí theo tháng (tìm theo pattern trong tên)
     */
    List<Fee> findByNameContainingAndIsRecurringTrueOrderByApartmentIdAsc(String namePattern);
}
