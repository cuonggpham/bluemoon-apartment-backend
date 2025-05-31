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

@Service
@RequiredArgsConstructor
public class PaymentRecordService {
    
    private final PaymentRecordRepository paymentRecordRepository;
    private final ResidentRepository residentRepository;
    private final FeeRepository feeRepository;
    private final ApartmentRepository apartmentRepository;
    
    @Transactional
    public PaymentRecordResponse createPaymentRecord(PaymentRecordRequest request) {
        // Validate payer exists
        Optional<Resident> payer = residentRepository.findById(request.getPayerId());
        if (payer.isEmpty()) {
            throw new RuntimeException("Payer not found with ID: " + request.getPayerId());
        }
        
        // Validate fee exists
        Optional<Fee> fee = feeRepository.findById(request.getFeeId());
        if (fee.isEmpty()) {
            throw new RuntimeException("Fee not found with ID: " + request.getFeeId());
        }
        
        Fee feeEntity = fee.get();
        Long apartmentId;
        
        // Handle apartment logic based on fee type
        if (feeEntity.getFeeTypeEnum() == FeeTypeEnum.Mandatory) {
            // For mandatory fees, use apartmentId from fee (must be set)
            if (feeEntity.getApartmentId() == null) {
                throw new RuntimeException("Mandatory fee must have an associated apartment");
            }
            apartmentId = feeEntity.getApartmentId();
        } else {
            // For voluntary fees, require apartmentId in request
            if (request.getApartmentId() == null) {
                throw new RuntimeException("Apartment ID is required for voluntary fee payments");
            }
            apartmentId = request.getApartmentId();
        }
        
        // Validate apartment exists
        Optional<Apartment> apartment = apartmentRepository.findById(apartmentId);
        if (apartment.isEmpty()) {
            throw new RuntimeException("Apartment not found with ID: " + apartmentId);
        }
        
        // Check for duplicate payment (include apartmentId in duplicate check)
        Optional<PaymentRecord> existingPayment = paymentRecordRepository
            .findByPayerIdAndFeeIdAndApartmentId(request.getPayerId(), request.getFeeId(), apartmentId);
        if (existingPayment.isPresent()) {
            throw new RuntimeException("Payment already exists for this payer, fee, and apartment");
        }
        
        // Create new payment record
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setPayerId(request.getPayerId());
        paymentRecord.setFeeId(request.getFeeId());
        paymentRecord.setApartmentId(apartmentId);
        paymentRecord.setPaymentDate(request.getPaymentDate());
        paymentRecord.setAmount(request.getAmount());
        paymentRecord.setNotes(request.getNotes());
        
        PaymentRecord savedRecord = paymentRecordRepository.save(paymentRecord);
        
        return mapToResponse(savedRecord, payer.get(), feeEntity, apartment.get());
    }
    
    public List<PaymentRecordResponse> getAllPaymentRecords() {
        List<PaymentRecord> records = paymentRecordRepository.findAllOrderByPaymentDateDesc();
        return records.stream()
            .map(this::mapToResponseWithLookup)
            .collect(Collectors.toList());
    }
    
    public List<PaymentRecordResponse> getPaymentsByPayer(Long payerId) {
        List<PaymentRecord> records = paymentRecordRepository.findByPayerId(payerId);
        return records.stream()
            .map(this::mapToResponseWithLookup)
            .collect(Collectors.toList());
    }
    
    public List<PaymentRecordResponse> getPaymentsByFee(Long feeId) {
        List<PaymentRecord> records = paymentRecordRepository.findByFeeId(feeId);
        return records.stream()
            .map(this::mapToResponseWithLookup)
            .collect(Collectors.toList());
    }
    
    public List<PaymentRecordResponse> getPaymentsByApartment(Long apartmentId) {
        List<PaymentRecord> records = paymentRecordRepository.findByApartmentId(apartmentId);
        return records.stream()
            .map(this::mapToResponseWithLookup)
            .collect(Collectors.toList());
    }
    
    public java.math.BigDecimal getTotalPaidAmountForFee(Long feeId) {
        List<PaymentRecord> records = paymentRecordRepository.findByFeeId(feeId);
        return records.stream()
            .map(PaymentRecord::getAmount)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
    
    private PaymentRecordResponse mapToResponse(PaymentRecord record, Resident payer, Fee fee, Apartment apartment) {
        PaymentRecordResponse response = new PaymentRecordResponse();
        response.setId(record.getId());
        response.setPayerId(record.getPayerId());
        response.setPayerName(payer.getName());
        response.setFeeId(record.getFeeId());
        response.setFeeName(fee.getName());
        response.setApartmentId(record.getApartmentId());
        response.setApartmentNumber(apartment.getAddressNumber().toString());
        response.setPaymentDate(record.getPaymentDate());
        response.setAmount(record.getAmount());
        response.setNotes(record.getNotes());
        response.setCreatedAt(record.getCreatedAt());
        response.setUpdatedAt(record.getUpdatedAt());
        return response;
    }
    
    private PaymentRecordResponse mapToResponseWithLookup(PaymentRecord record) {
        Optional<Resident> payer = residentRepository.findById(record.getPayerId());
        Optional<Fee> fee = feeRepository.findById(record.getFeeId());
        Optional<Apartment> apartment = apartmentRepository.findById(record.getApartmentId());
        
        PaymentRecordResponse response = new PaymentRecordResponse();
        response.setId(record.getId());
        response.setPayerId(record.getPayerId());
        response.setPayerName(payer.map(Resident::getName).orElse("Unknown"));
        response.setFeeId(record.getFeeId());
        response.setFeeName(fee.map(Fee::getName).orElse("Unknown"));
        response.setApartmentId(record.getApartmentId());
        response.setApartmentNumber(apartment.map(apt -> apt.getAddressNumber().toString()).orElse("Unknown"));
        response.setPaymentDate(record.getPaymentDate());
        response.setAmount(record.getAmount());
        response.setNotes(record.getNotes());
        response.setCreatedAt(record.getCreatedAt());
        response.setUpdatedAt(record.getUpdatedAt());
        return response;
    }
} 