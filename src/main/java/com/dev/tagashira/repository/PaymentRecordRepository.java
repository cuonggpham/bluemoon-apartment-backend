package com.dev.tagashira.repository;

import com.dev.tagashira.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {
    
    // Check if a payment already exists for a specific payer and fee
    @Query("SELECT pr FROM PaymentRecord pr WHERE pr.payerId = :payerId AND pr.feeId = :feeId")
    Optional<PaymentRecord> findByPayerIdAndFeeId(@Param("payerId") Long payerId, @Param("feeId") Long feeId);
    
    // Check if a payment already exists for a specific payer, fee, and apartment
    @Query("SELECT pr FROM PaymentRecord pr WHERE pr.payerId = :payerId AND pr.feeId = :feeId AND pr.apartmentId = :apartmentId")
    Optional<PaymentRecord> findByPayerIdAndFeeIdAndApartmentId(@Param("payerId") Long payerId, @Param("feeId") Long feeId, @Param("apartmentId") Long apartmentId);
    
    // Get all payments for a specific payer
    @Query("SELECT pr FROM PaymentRecord pr WHERE pr.payerId = :payerId ORDER BY pr.paymentDate DESC")
    List<PaymentRecord> findByPayerId(@Param("payerId") Long payerId);
    
    // Get all payments for a specific fee
    @Query("SELECT pr FROM PaymentRecord pr WHERE pr.feeId = :feeId ORDER BY pr.paymentDate DESC")
    List<PaymentRecord> findByFeeId(@Param("feeId") Long feeId);
    
    // Get all payments for a specific apartment
    @Query("SELECT pr FROM PaymentRecord pr WHERE pr.apartmentId = :apartmentId ORDER BY pr.paymentDate DESC")
    List<PaymentRecord> findByApartmentId(@Param("apartmentId") Long apartmentId);
    
    // Get all payments ordered by date
    @Query("SELECT pr FROM PaymentRecord pr ORDER BY pr.paymentDate DESC")
    List<PaymentRecord> findAllOrderByPaymentDateDesc();
} 