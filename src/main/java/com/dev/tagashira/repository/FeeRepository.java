package com.dev.tagashira.repository;

import com.dev.tagashira.constant.FeeTypeEnum;
import com.dev.tagashira.entity.Fee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeeRepository extends JpaRepository<Fee, Long>, JpaSpecificationExecutor<Fee> {
    /**
     * Tìm phí theo tên và apartment ID (để kiểm tra duplicate)
     */
    @Query("SELECT f FROM Fee f WHERE f.name = :name AND f.apartment.addressNumber = :apartmentId")
    Optional<Fee> findByNameAndApartmentId(@Param("name") String name, @Param("apartmentId") Long apartmentId);
    
    /**
     * Lấy tất cả phí chưa thanh toán của một apartment
     */
    @Query("SELECT f FROM Fee f WHERE f.apartment.addressNumber = :apartmentId AND f.isRecurring = true AND f.isActive = true ORDER BY f.createdAt DESC")
    List<Fee> findByApartmentIdAndIsRecurringTrueAndIsActiveTrueOrderByCreatedAtDesc(@Param("apartmentId") Long apartmentId);
    
    /**
     * Lấy phí theo tháng (tìm theo pattern trong tên)
     */
    @Query("SELECT f FROM Fee f WHERE f.name LIKE %:namePattern% AND f.isRecurring = true ORDER BY f.apartment.addressNumber ASC")
    List<Fee> findByNameContainingAndIsRecurringTrueOrderByApartmentIdAsc(@Param("namePattern") String namePattern);
    
    /**
     */
    @Query("SELECT f FROM Fee f WHERE f.apartment.addressNumber = :apartmentId")
    List<Fee> findByApartmentId(@Param("apartmentId") Long apartmentId);
    

    @Query("SELECT f FROM Fee f WHERE f.feeTypeEnum = :feeType AND f.apartment.addressNumber = :apartmentId AND f.isActive = true")
    List<Fee> findByFeeTypeAndApartmentId(@Param("feeType") FeeTypeEnum feeType, @Param("apartmentId") Long apartmentId);
    

    @Query("SELECT f FROM Fee f WHERE f.apartment.addressNumber = :apartmentId AND f.isActive = true AND f.paymentRecord IS NULL")
    List<Fee> findUnpaidFeesByApartmentId(@Param("apartmentId") Long apartmentId);
}
