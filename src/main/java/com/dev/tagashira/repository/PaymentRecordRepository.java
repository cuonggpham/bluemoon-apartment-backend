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
    
    // Get all payments for a specific payer
    @Query("SELECT pr FROM PaymentRecord pr WHERE pr.payer.id = :payerId ORDER BY pr.paymentDate DESC")
    List<PaymentRecord> findByPayerId(@Param("payerId") Long payerId);
    
    // Get payment for a specific fee (should only be one since Fee 1:1 PaymentRecord)
    @Query("SELECT pr FROM PaymentRecord pr WHERE pr.fee.id = :feeId")
    Optional<PaymentRecord> findByFeeId(@Param("feeId") Long feeId);
    
    // Get all payments for a specific apartment through fee relationship
    @Query("SELECT pr FROM PaymentRecord pr WHERE pr.fee.apartment.addressNumber = :apartmentId ORDER BY pr.paymentDate DESC")
    List<PaymentRecord> findByApartmentId(@Param("apartmentId") Long apartmentId);
    
    // Get all payments ordered by date
    @Query("SELECT pr FROM PaymentRecord pr ORDER BY pr.paymentDate DESC")
    List<PaymentRecord> findAllOrderByPaymentDateDesc();
} 