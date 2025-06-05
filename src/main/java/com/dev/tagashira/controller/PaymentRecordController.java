package com.dev.tagashira.controller;

import com.dev.tagashira.dto.request.PaymentRecordRequest;
import com.dev.tagashira.dto.response.FeeResponse;
import com.dev.tagashira.dto.response.PaymentRecordResponse;
import com.dev.tagashira.service.PaymentRecordService;
import com.dev.tagashira.service.FeeCrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/payment-records")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class PaymentRecordController {
    
    private final PaymentRecordService paymentRecordService;
    private final FeeCrudService feeCrudService;
    
    @PostMapping
    public ResponseEntity<PaymentRecordResponse> createPaymentRecord(@Valid @RequestBody PaymentRecordRequest request) {
        PaymentRecordResponse response = paymentRecordService.createPaymentRecord(request);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<PaymentRecordResponse> updatePaymentRecord(@PathVariable Long id, @Valid @RequestBody PaymentRecordRequest request) {
        PaymentRecordResponse response = paymentRecordService.updatePaymentRecord(id, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<PaymentRecordResponse>> getAllPaymentRecords() {
        List<PaymentRecordResponse> records = paymentRecordService.getAllPaymentRecords();
        return ResponseEntity.ok(records);
    }
    
    @GetMapping("/payer/{payerId}")
    public ResponseEntity<List<PaymentRecordResponse>> getPaymentsByPayer(@PathVariable Long payerId) {
        List<PaymentRecordResponse> records = paymentRecordService.getPaymentsByPayer(payerId);
        return ResponseEntity.ok(records);
    }
    
    @GetMapping("/fee/{feeId}")
    public ResponseEntity<PaymentRecordResponse> getPaymentByFee(@PathVariable Long feeId) {
        Optional<PaymentRecordResponse> record = paymentRecordService.getPaymentByFee(feeId);
        return record.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/fee/{feeId}/total-amount")
    public ResponseEntity<BigDecimal> getTotalPaidAmountForFee(@PathVariable Long feeId) {
        BigDecimal totalAmount = paymentRecordService.getTotalPaidAmountForFee(feeId);
        return ResponseEntity.ok(totalAmount);
    }
    
    @GetMapping("/apartment/{apartmentId}")
    public ResponseEntity<List<PaymentRecordResponse>> getPaymentsByApartment(@PathVariable Long apartmentId) {
        List<PaymentRecordResponse> records = paymentRecordService.getPaymentsByApartment(apartmentId);
        return ResponseEntity.ok(records);
    }

    /**
     * Create payment for recurring fee (monthly fee) - uses the unified payment creation method
     */
    @PostMapping("/recurring-fees")
    public ResponseEntity<PaymentRecordResponse> createRecurringFeePayment(
            @RequestParam Long payerId,
            @RequestParam Long feeId) {
        
        // Get fee details to create proper payment request
        FeeResponse fee = feeCrudService.findById(feeId);
        
        // Create payment request using the unified method
        PaymentRecordRequest request = new PaymentRecordRequest();
        request.setPayerId(payerId);
        request.setFeeId(feeId);
        request.setAmount(fee.getAmount());
        request.setPaymentDate(LocalDate.now());
        request.setNotes("Thanh toán " + fee.getName());

        PaymentRecordResponse response = paymentRecordService.createPaymentRecord(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all recurring fee payments
     */
    @GetMapping("/recurring-fees")
    public ResponseEntity<List<PaymentRecordResponse>> getRecurringFeePayments() {
        List<PaymentRecordResponse> records = paymentRecordService.getRecurringFeePayments();
        return ResponseEntity.ok(records);
    }
    
    /**
     * Get recurring fee payments by apartment
     */
    @GetMapping("/recurring-fees/apartment/{apartmentId}")
    public ResponseEntity<List<PaymentRecordResponse>> getRecurringFeePaymentsByApartment(@PathVariable Long apartmentId) {
        List<PaymentRecordResponse> records = paymentRecordService.getRecurringFeePaymentsByApartment(apartmentId);
        return ResponseEntity.ok(records);
    }
} 