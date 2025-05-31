package com.dev.tagashira.controller;

import com.dev.tagashira.dto.request.PaymentRecordRequest;
import com.dev.tagashira.dto.response.PaymentRecordResponse;
import com.dev.tagashira.service.PaymentRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment-records")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentRecordController {
    
    private final PaymentRecordService paymentRecordService;
    
    @PostMapping
    public PaymentRecordResponse createPaymentRecord(@RequestBody PaymentRecordRequest request) {
        return paymentRecordService.createPaymentRecord(request);
    }
    
    @GetMapping
    public List<PaymentRecordResponse> getAllPaymentRecords() {
        return paymentRecordService.getAllPaymentRecords();
    }
    
    @GetMapping("/payer/{payerId}")
    public List<PaymentRecordResponse> getPaymentsByPayer(@PathVariable Long payerId) {
        return paymentRecordService.getPaymentsByPayer(payerId);
    }
    
    @GetMapping("/fee/{feeId}")
    public List<PaymentRecordResponse> getPaymentsByFee(@PathVariable Long feeId) {
        return paymentRecordService.getPaymentsByFee(feeId);
    }
    
    @GetMapping("/fee/{feeId}/total")
    public java.math.BigDecimal getTotalPaidAmountForFee(@PathVariable Long feeId) {
        return paymentRecordService.getTotalPaidAmountForFee(feeId);
    }
    
    @GetMapping("/apartment/{apartmentId}")
    public List<PaymentRecordResponse> getPaymentsByApartment(@PathVariable Long apartmentId) {
        return paymentRecordService.getPaymentsByApartment(apartmentId);
    }
} 