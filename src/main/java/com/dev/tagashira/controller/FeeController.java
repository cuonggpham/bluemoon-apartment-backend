package com.dev.tagashira.controller;

import com.dev.tagashira.dto.request.FeeCreateRequest;
import com.dev.tagashira.dto.response.ApiResponse;
import com.dev.tagashira.dto.response.PaginatedResponse;
import com.dev.tagashira.dto.response.FeeResponse;
import com.dev.tagashira.entity.Fee;
import com.dev.tagashira.constant.FeeTypeEnum;
import com.dev.tagashira.constant.VehicleEnum;
import com.dev.tagashira.service.FeeService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/fees")
@CrossOrigin(origins = "http://localhost:5173")
public class FeeController {
    private final FeeService feeService;

    //fetch all fees
    @GetMapping()
    public ResponseEntity<PaginatedResponse<FeeResponse>> getAllFees(@Filter Specification<Fee> spec,
                                                             @RequestParam(value = "page", defaultValue = "1") int page,
                                                             @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        PaginatedResponse<FeeResponse> feeResponses = this.feeService.fetchAllFees(spec, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(feeResponses);
    }

    //fetch fee by feeCode
    @GetMapping("/{id}")
    public ResponseEntity<FeeResponse> getFeeByFeeCode(@PathVariable("id") Long id) {
        FeeResponse fetchFee = this.feeService.getFeeResponseById(id);
        return ResponseEntity.status(HttpStatus.OK).body(fetchFee);
    }

    //create new fee
    @PostMapping()
    public ResponseEntity<FeeResponse> createFee(@Valid @RequestBody FeeCreateRequest apiFee) {
        FeeResponse fee = this.feeService.createFee(apiFee);
        return ResponseEntity.status(HttpStatus.CREATED).body(fee);
    }

    //update fee
    @PutMapping()
    public ResponseEntity<FeeResponse> updateFee(@RequestBody Fee apiFee) {
        FeeResponse fee = this.feeService.updateFee(apiFee);
        return ResponseEntity.status(HttpStatus.OK).body(fee);
    }

    //Delete resident by feeCode
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteFee(@PathVariable("id") Long id) {
        ApiResponse<String> response = this.feeService.deleteFee(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Tạo phí hàng tháng cho tất cả apartment
     */
    @PostMapping("/monthly/generate")
    public ResponseEntity<List<Fee>> generateMonthlyFees(
            @RequestParam FeeTypeEnum feeType,
            @RequestParam String billingMonth, // Format: YYYY-MM
            @RequestParam(required = false) BigDecimal unitPricePerSqm) {
        
        List<Fee> generatedFees = feeService.generateMonthlyFeesForAllApartments(feeType, billingMonth, unitPricePerSqm);
        return ResponseEntity.ok(generatedFees);
    }
    
    /**
     * Lấy phí chưa thanh toán của một apartment
     */
    @GetMapping("/monthly/unpaid/apartment/{apartmentId}")
    public ResponseEntity<List<Fee>> getUnpaidMonthlyFeesByApartment(@PathVariable Long apartmentId) {
        List<Fee> unpaidFees = feeService.getUnpaidMonthlyFeesByApartment(apartmentId);
        return ResponseEntity.ok(unpaidFees);
    }
    
    /**
     * Lấy tất cả phí theo tháng
     */
    @GetMapping("/monthly/month/{billingMonth}")
    public ResponseEntity<List<Fee>> getMonthlyFeesByMonth(@PathVariable String billingMonth) {
        List<Fee> monthlyFees = feeService.getMonthlyFeesByMonth(billingMonth);
        return ResponseEntity.ok(monthlyFees);
    }
    
    /**
     * Vô hiệu hóa phí (khi đã thanh toán hoặc hủy)
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateFee(@PathVariable Long id) {
        feeService.deactivateFee(id);
        
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(HttpStatus.OK.value());
        response.setMessage("Fee deactivated successfully");
        return ResponseEntity.ok(response);
    }
}
