package com.dev.tagashira.service;

import com.dev.tagashira.dto.request.PaymentRecordRequest;
import com.dev.tagashira.dto.response.PaymentRecordResponse;
import com.dev.tagashira.entity.Apartment;
import com.dev.tagashira.entity.Fee;
import com.dev.tagashira.entity.PaymentRecord;
import com.dev.tagashira.entity.Resident;
import com.dev.tagashira.repository.ApartmentRepository;
import com.dev.tagashira.repository.FeeRepository;
import com.dev.tagashira.repository.PaymentRecordRepository;
import com.dev.tagashira.repository.ResidentRepository;
import com.dev.tagashira.constant.FeeTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PaymentRecordService {
    
    private final PaymentRecordRepository paymentRecordRepository;
    private final ResidentRepository residentRepository;
    private final FeeRepository feeRepository;
    private final ApartmentRepository apartmentRepository;
    private final FeeCrudService feeCrudService;
    
    @Transactional
    public PaymentRecordResponse createPaymentRecord(PaymentRecordRequest request) {
        // Validate payer exists
        Resident payer = residentRepository.findById(request.getPayerId())
            .orElseThrow(() -> new RuntimeException("Payer not found with ID: " + request.getPayerId()));
        
        // Validate fee exists
        Fee fee = feeRepository.findById(request.getFeeId())
            .orElseThrow(() -> new RuntimeException("Fee not found with ID: " + request.getFeeId()));
        
        // All fees now have apartmentId, use it directly
        if (fee.getApartmentId() == null) {
            throw new RuntimeException("Fee must have an associated apartment");
        }
        
        Long apartmentId = fee.getApartmentId();
        
        // Validate apartment exists
        Apartment apartment = apartmentRepository.findById(apartmentId)
            .orElseThrow(() -> new RuntimeException("Apartment not found with ID: " + apartmentId));
        
        // Check if fee has already been paid (for non-voluntary fees)
        if (fee.getFeeTypeEnum() != FeeTypeEnum.VOLUNTARY && fee.isPaid()) {
            throw new RuntimeException("Fee has already been paid in full");
        }
        
        // Validate payment amount based on fee type
        validatePaymentAmount(request.getAmount(), fee);
        
        // For voluntary fees, check if this is an additional payment
        PaymentRecord existingPayment = null;
        if (fee.getFeeTypeEnum() == FeeTypeEnum.VOLUNTARY) {
            Optional<PaymentRecord> existing = paymentRecordRepository.findByFeeId(request.getFeeId());
            if (existing.isPresent()) {
                existingPayment = existing.get();
                // Update existing payment amount
                BigDecimal newTotalAmount = existingPayment.getAmount().add(request.getAmount());
                existingPayment.setAmount(newTotalAmount);
                existingPayment.setPaymentDate(request.getPaymentDate());
                existingPayment.setNotes(request.getNotes());
                existingPayment.setUpdatedAt(Instant.now());
                
                PaymentRecord savedRecord = paymentRecordRepository.save(existingPayment);
                return mapToResponse(savedRecord, payer, fee, apartment);
            }
        }
        
        // Create new payment record using relationships
        PaymentRecord paymentRecord = PaymentRecord.builder()
            .payer(payer)
            .fee(fee)
            .apartment(apartment)
            .paymentDate(request.getPaymentDate())
            .amount(request.getAmount())
            .notes(request.getNotes())
            .build();
        
        PaymentRecord savedRecord = paymentRecordRepository.save(paymentRecord);
        
        // If this is a recurring fee (monthly fee) and paid in full, deactivate it
        if (fee.getIsRecurring() && request.getAmount().compareTo(fee.getAmount()) >= 0) {
            feeCrudService.deactivate(request.getFeeId());
        }
        
        return mapToResponse(savedRecord, payer, fee, apartment);
    }
    
    /**
     * Validate payment amount based on fee type rules
     */
    private void validatePaymentAmount(BigDecimal paymentAmount, Fee fee) {
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Payment amount must be greater than zero");
        }
        
        if (fee.getFeeTypeEnum() == FeeTypeEnum.VOLUNTARY) {
            // For voluntary fees: minimum amount is the fee amount (can pay more)
            if (paymentAmount.compareTo(fee.getAmount()) < 0) {
                throw new RuntimeException(
                    String.format("Voluntary fee requires minimum payment of %s VND, but received %s VND", 
                        fee.getAmount(), paymentAmount)
                );
            }
        } else {
            // For other fees: maximum amount is the fee amount (cannot pay more than required)
            if (paymentAmount.compareTo(fee.getAmount()) > 0) {
                throw new RuntimeException(
                    String.format("Payment amount %s VND exceeds the required fee amount of %s VND", 
                        paymentAmount, fee.getAmount())
                );
            }
        }
    }
    
    /**
     * Update existing payment record
     */
    @Transactional
    public PaymentRecordResponse updatePaymentRecord(Long paymentId, PaymentRecordRequest request) {
        // Find existing payment record
        PaymentRecord existingPayment = paymentRecordRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment record not found with ID: " + paymentId));
        
        Fee fee = existingPayment.getFee();
        
        // Validate new payment amount
        validatePaymentAmount(request.getAmount(), fee);
        
        // Update payment record
        existingPayment.setAmount(request.getAmount());
        existingPayment.setPaymentDate(request.getPaymentDate());
        existingPayment.setNotes(request.getNotes());
        existingPayment.setUpdatedAt(Instant.now());
        
        PaymentRecord savedRecord = paymentRecordRepository.save(existingPayment);
        
        // Check if fee should be deactivated after update
        if (fee.getIsRecurring() && request.getAmount().compareTo(fee.getAmount()) >= 0) {
            feeCrudService.deactivate(fee.getId());
        }
        
        return mapToResponseFromEntity(savedRecord);
    }
    
    public List<PaymentRecordResponse> getAllPaymentRecords() {
        List<PaymentRecord> records = paymentRecordRepository.findAllOrderByPaymentDateDesc();
        return records.stream()
            .map(this::mapToResponseFromEntity)
            .collect(Collectors.toList());
    }
    
    public List<PaymentRecordResponse> getPaymentsByPayer(Long payerId) {
        List<PaymentRecord> records = paymentRecordRepository.findByPayerId(payerId);
        return records.stream()
            .map(this::mapToResponseFromEntity)
            .collect(Collectors.toList());
    }
    
    public Optional<PaymentRecordResponse> getPaymentByFee(Long feeId) {
        Optional<PaymentRecord> record = paymentRecordRepository.findByFeeId(feeId);
        return record.map(this::mapToResponseFromEntity);
    }
    
    public List<PaymentRecordResponse> getPaymentsByApartment(Long apartmentId) {
        List<PaymentRecord> records = paymentRecordRepository.findByApartmentId(apartmentId);
        return records.stream()
            .map(this::mapToResponseFromEntity)
            .collect(Collectors.toList());
    }
    
    public java.math.BigDecimal getTotalPaidAmountForFee(Long feeId) {
        Optional<PaymentRecord> record = paymentRecordRepository.findByFeeId(feeId);
        return record.map(PaymentRecord::getAmount).orElse(java.math.BigDecimal.ZERO);
    }
    
    /**
     * Lấy tất cả payments của Monthly Fees (recurring fees)
     */
    public List<PaymentRecordResponse> getRecurringFeePayments() {
        List<PaymentRecord> records = paymentRecordRepository.findAllOrderByPaymentDateDesc();
        
        return records.stream()
            .filter(record -> record.getFee().getIsRecurring()) // Filter at record level
            .map(this::mapToResponseFromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy payments của Monthly Fees theo apartment
     */
    public List<PaymentRecordResponse> getRecurringFeePaymentsByApartment(Long apartmentId) {
        List<PaymentRecord> records = paymentRecordRepository.findByApartmentId(apartmentId);
        
        return records.stream()
            .filter(record -> record.getFee().getIsRecurring()) // Filter at record level
            .map(this::mapToResponseFromEntity)
            .collect(Collectors.toList());
    }
    
    private PaymentRecordResponse mapToResponse(PaymentRecord record, Resident payer, Fee fee, Apartment apartment) {
        PaymentRecordResponse response = new PaymentRecordResponse();
        response.setId(record.getId());
        response.setPayerId(payer.getId());
        response.setPayerName(payer.getName());
        response.setFeeId(fee.getId());
        response.setFeeName(fee.getName());
        response.setApartmentId(apartment.getAddressNumber());
        response.setApartmentNumber(apartment.getAddressNumber().toString());
        response.setPaymentDate(record.getPaymentDate());
        response.setAmount(record.getAmount());
        response.setNotes(record.getNotes());
        response.setCreatedAt(record.getCreatedAt());
        response.setUpdatedAt(record.getUpdatedAt());
        
        // Calculate debt status
        calculateDebtStatus(response, record.getAmount(), fee);
        
        return response;
    }
    
    private PaymentRecordResponse mapToResponseFromEntity(PaymentRecord record) {
        PaymentRecordResponse response = new PaymentRecordResponse();
        response.setId(record.getId());
        response.setPayerId(record.getPayer().getId());
        response.setPayerName(record.getPayer().getName());
        response.setFeeId(record.getFee().getId());
        response.setFeeName(record.getFee().getName());
        response.setApartmentId(record.getApartment().getAddressNumber());
        response.setApartmentNumber(record.getApartment().getAddressNumber().toString());
        response.setPaymentDate(record.getPaymentDate());
        response.setAmount(record.getAmount());
        response.setNotes(record.getNotes());
        response.setCreatedAt(record.getCreatedAt());
        response.setUpdatedAt(record.getUpdatedAt());
        
        // Calculate debt status
        calculateDebtStatus(response, record.getAmount(), record.getFee());
        
        return response;
    }
    
    /**
     * Calculate debt status for payment response
     */
    private void calculateDebtStatus(PaymentRecordResponse response, BigDecimal paidAmount, Fee fee) {
        if (fee.getFeeTypeEnum() == FeeTypeEnum.VOLUNTARY) {
            // For voluntary fees, any payment >= minimum is considered fully paid
            response.setIsFullyPaid(paidAmount.compareTo(fee.getAmount()) >= 0);
            response.setRemainingAmount(BigDecimal.ZERO);
        } else {
            // For other fees, check if paid amount equals fee amount
            response.setIsFullyPaid(paidAmount.compareTo(fee.getAmount()) >= 0);
            BigDecimal remaining = fee.getAmount().subtract(paidAmount);
            response.setRemainingAmount(remaining.max(BigDecimal.ZERO));
        }
    }
} 