package com.dev.tagashira.controller;

import com.dev.tagashira.dto.request.PaymentRecordRequest;
import com.dev.tagashira.dto.response.FeeResponse;
import com.dev.tagashira.dto.response.PaymentRecordResponse;
import com.dev.tagashira.service.PaymentRecordService;
import com.dev.tagashira.service.FeeCrudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@Tag(name = "Payment Record Management", description = "APIs for managing payment records")
@Slf4j
public class PaymentRecordController {
    
    private final PaymentRecordService paymentRecordService;
    private final FeeCrudService feeCrudService;
    
    @PostMapping
    @Operation(summary = "Create payment record", description = "Create a new payment record")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<PaymentRecordResponse> createPaymentRecord(@Valid @RequestBody PaymentRecordRequest request) {
        log.info("POST /api/v1/payment-records - Creating payment record");
        PaymentRecordResponse response = paymentRecordService.createPaymentRecord(request);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update payment record", description = "Update an existing payment record")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<PaymentRecordResponse> updatePaymentRecord(@PathVariable Long id, @Valid @RequestBody PaymentRecordRequest request) {
        log.info("PUT /api/v1/payment-records/{} - Updating payment record", id);
        PaymentRecordResponse response = paymentRecordService.updatePaymentRecord(id, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all payment records", description = "Retrieve all payment records")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'MANAGER')")
    public ResponseEntity<List<PaymentRecordResponse>> getAllPaymentRecords() {
        log.info("GET /api/v1/payment-records - Fetching all payment records");
        List<PaymentRecordResponse> records = paymentRecordService.getAllPaymentRecords();
        return ResponseEntity.ok(records);
    }
    
    @GetMapping("/payer/{payerId}")
    @Operation(summary = "Get payments by payer", description = "Retrieve payment records by payer ID")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'MANAGER')")
    public ResponseEntity<List<PaymentRecordResponse>> getPaymentsByPayer(@PathVariable Long payerId) {
        log.info("GET /api/v1/payment-records/payer/{} - Fetching payments by payer", payerId);
        List<PaymentRecordResponse> records = paymentRecordService.getPaymentsByPayer(payerId);
        return ResponseEntity.ok(records);
    }
    
    @GetMapping("/fee/{feeId}")
    @Operation(summary = "Get payment by fee", description = "Retrieve payment record by fee ID")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'MANAGER')")
    public ResponseEntity<PaymentRecordResponse> getPaymentByFee(@PathVariable Long feeId) {
        log.info("GET /api/v1/payment-records/fee/{} - Fetching payment by fee", feeId);
        Optional<PaymentRecordResponse> record = paymentRecordService.getPaymentByFee(feeId);
        return record.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/fee/{feeId}/total-amount")
    @Operation(summary = "Get total paid amount for fee", description = "Get total amount paid for a specific fee")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'MANAGER')")
    public ResponseEntity<BigDecimal> getTotalPaidAmountForFee(@PathVariable Long feeId) {
        log.info("GET /api/v1/payment-records/fee/{}/total-amount - Fetching total paid amount", feeId);
        BigDecimal totalAmount = paymentRecordService.getTotalPaidAmountForFee(feeId);
        return ResponseEntity.ok(totalAmount);
    }
    
    @GetMapping("/apartment/{apartmentId}")
    @Operation(summary = "Get payments by apartment", description = "Retrieve payment records by apartment ID")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'MANAGER')")
    public ResponseEntity<List<PaymentRecordResponse>> getPaymentsByApartment(@PathVariable Long apartmentId) {
        log.info("GET /api/v1/payment-records/apartment/{} - Fetching payments by apartment", apartmentId);
        List<PaymentRecordResponse> records = paymentRecordService.getPaymentsByApartment(apartmentId);
        return ResponseEntity.ok(records);
    }

    /**
     * Create payment for recurring fee (monthly fee) - uses the unified payment creation method
     */
    @PostMapping("/recurring-fees")
    @Operation(summary = "Create recurring fee payment", description = "Create payment for recurring fees")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<PaymentRecordResponse> createRecurringFeePayment(
            @RequestParam Long payerId,
            @RequestParam Long feeId) {
        
        log.info("POST /api/v1/payment-records/recurring-fees - Creating recurring fee payment");
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
    @Operation(summary = "Get recurring fee payments", description = "Retrieve all recurring fee payments")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'MANAGER')")
    public ResponseEntity<List<PaymentRecordResponse>> getRecurringFeePayments() {
        log.info("GET /api/v1/payment-records/recurring-fees - Fetching recurring fee payments");
        List<PaymentRecordResponse> records = paymentRecordService.getRecurringFeePayments();
        return ResponseEntity.ok(records);
    }
    
    /**
     * Get recurring fee payments by apartment
     */
    @GetMapping("/recurring-fees/apartment/{apartmentId}")
    @Operation(summary = "Get recurring fee payments by apartment", description = "Retrieve recurring fee payments by apartment")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'MANAGER')")
    public ResponseEntity<List<PaymentRecordResponse>> getRecurringFeePaymentsByApartment(@PathVariable Long apartmentId) {
        log.info("GET /api/v1/payment-records/recurring-fees/apartment/{} - Fetching recurring fee payments by apartment", apartmentId);
        List<PaymentRecordResponse> records = paymentRecordService.getRecurringFeePaymentsByApartment(apartmentId);
        return ResponseEntity.ok(records);
    }
}