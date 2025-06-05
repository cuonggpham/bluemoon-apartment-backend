package com.dev.tagashira.controller;

import com.dev.tagashira.constant.FeeTypeEnum;
import com.dev.tagashira.dto.request.FeeCreateRequest;
import com.dev.tagashira.dto.response.ApiResponse;
import com.dev.tagashira.dto.response.FeeResponse;
import com.dev.tagashira.dto.response.PaginatedResponse;
import com.dev.tagashira.entity.Fee;
import com.dev.tagashira.exception.DuplicateFeeException;
import com.dev.tagashira.exception.NoVehicleException;
import com.dev.tagashira.service.FeeCrudService;
import com.dev.tagashira.service.MonthlyFeeGeneratorService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fees")
@CrossOrigin(origins = "http://localhost:5173")
public class FeeController {

    private final FeeCrudService feeCrudService;
    private final MonthlyFeeGeneratorService monthlyGenService;

    @GetMapping
    public ResponseEntity<PaginatedResponse<FeeResponse>> getAllFees(
            @Filter Specification<Fee> spec,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page - 1, size);
        return ResponseEntity.ok(feeCrudService.findAll(spec, pageable));
    }

    @GetMapping("/all")
    public ResponseEntity<List<FeeResponse>> getAllFeesNoPagination(@Filter Specification<Fee> spec) {
        return ResponseEntity.ok(feeCrudService.findAll(spec));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeeResponse> getFee(@PathVariable Long id) {
        return ResponseEntity.ok(feeCrudService.findById(id));
    }

    @PostMapping
    public ResponseEntity<FeeResponse> createFee(@Valid @RequestBody FeeCreateRequest req) {
        return new ResponseEntity<>(feeCrudService.create(req), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeeResponse> updateFee(@PathVariable Long id, @RequestBody Fee req) {
        // đưa id từ path vào payload để đảm bảo khớp
        req.setId(id);
        return ResponseEntity.ok(feeCrudService.update(req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteFee(@PathVariable Long id) {
        return ResponseEntity.ok(feeCrudService.delete(id));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateFee(@PathVariable Long id) {
        feeCrudService.deactivate(id);

        ApiResponse<String> res = new ApiResponse<>();
        res.setCode(HttpStatus.OK.value());
        res.setMessage("Fee deactivated successfully");
        return ResponseEntity.ok(res);
    }


    @PostMapping("/monthly/generate")
    public ResponseEntity<List<Fee>> generateMonthlyFees(
            @RequestParam FeeTypeEnum feeType,
            @RequestParam String billingMonth,
            @RequestParam(required = false) BigDecimal unitPricePerSqm,
            @RequestParam(required = false) String customFeeName) {

        YearMonth ym = YearMonth.parse(billingMonth);     // đảm bảo format hợp lệ
        List<Fee> fees = monthlyGenService.generateForAll(
                feeType, ym, unitPricePerSqm, customFeeName);
        return ResponseEntity.ok(fees);
    }

    @GetMapping("/monthly/unpaid/apartment/{apartmentId}")
    public ResponseEntity<List<Fee>> getUnpaidMonthlyFees(@PathVariable Long apartmentId) {
        return ResponseEntity.ok(monthlyGenService
                .getUnpaidMonthlyFeesByApartment(apartmentId));
    }

    @GetMapping("/monthly/month/{billingMonth}")
    public ResponseEntity<List<Fee>> getMonthlyFeesByMonth(@PathVariable String billingMonth) {
        return ResponseEntity.ok(monthlyGenService
                .getMonthlyFeesByMonth(billingMonth));
    }
}
