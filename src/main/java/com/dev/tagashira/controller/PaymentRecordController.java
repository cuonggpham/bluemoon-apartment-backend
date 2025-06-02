package com.dev.tagashira.controller;

import com.dev.tagashira.dto.request.PaymentRecordRequest;
import com.dev.tagashira.dto.response.PaymentRecordResponse;
import com.dev.tagashira.service.PaymentRecordService;
import com.dev.tagashira.service.FeeService;
import com.dev.tagashira.entity.Fee;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payment-records")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class PaymentRecordController {
    
    private final PaymentRecordService paymentRecordService;
    private final FeeService feeService;
    
    @PostMapping
    public ResponseEntity<PaymentRecordResponse> createPaymentRecord(@RequestBody PaymentRecordRequest request) {
        PaymentRecordResponse response = paymentRecordService.createPaymentRecord(request);
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
    public ResponseEntity<List<PaymentRecordResponse>> getPaymentsByFee(@PathVariable Long feeId) {
        List<PaymentRecordResponse> records = paymentRecordService.getPaymentsByFee(feeId);
        return ResponseEntity.ok(records);
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
        Fee fee = feeService.fetchFeeById(feeId);
        
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