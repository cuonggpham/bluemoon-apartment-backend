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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fees")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Fee Management", description = "APIs for managing fees and billing")
@Slf4j
public class FeeController {

    private final FeeCrudService feeCrudService;
    private final MonthlyFeeGeneratorService monthlyGenService;

    @GetMapping
    @Operation(summary = "Get all fees", description = "Retrieve all fees with pagination and filtering")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'MANAGER')")
    public ResponseEntity<PaginatedResponse<FeeResponse>> getAllFees(
            @Filter Specification<Fee> spec,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("GET /api/v1/fees - Fetching fees with pagination");
        Pageable pageable = PageRequest.of(page - 1, size);
        return ResponseEntity.ok(feeCrudService.findAll(spec, pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all fees without pagination", description = "Retrieve all fees without pagination")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'MANAGER')")
    public ResponseEntity<List<FeeResponse>> getAllFeesNoPagination(@Filter Specification<Fee> spec) {
        log.info("GET /api/v1/fees/all - Fetching all fees without pagination");
        return ResponseEntity.ok(feeCrudService.findAll(spec));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get fee by ID", description = "Retrieve a specific fee by its ID")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'MANAGER')")
    public ResponseEntity<FeeResponse> getFee(@PathVariable Long id) {
        log.info("GET /api/v1/fees/{} - Fetching fee by ID", id);
        return ResponseEntity.ok(feeCrudService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create new fee", description = "Create a new fee")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<FeeResponse> createFee(@Valid @RequestBody FeeCreateRequest req) {
        log.info("POST /api/v1/fees - Creating new fee");
        return new ResponseEntity<>(feeCrudService.create(req), HttpStatus.CREATED);
    }

    @PostMapping("/voluntary/create-for-all")
    @Operation(summary = "Create voluntary fee for all apartments", description = "Create a voluntary fee for all apartments")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<List<FeeResponse>>> createVoluntaryFeeForAllApartments(@Valid @RequestBody FeeCreateRequest req) {
        log.info("POST /api/v1/fees/voluntary/create-for-all - Creating voluntary fee for all apartments");
        List<FeeResponse> createdFees = feeCrudService.createVoluntaryFeeForAllApartments(req);
        
        ApiResponse<List<FeeResponse>> response = new ApiResponse<>();
        response.setCode(HttpStatus.CREATED.value());
        response.setMessage("Voluntary fees created for " + createdFees.size() + " apartments");
        response.setData(createdFees);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update fee", description = "Update an existing fee")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<FeeResponse> updateFee(@PathVariable Long id, @RequestBody Fee req) {
        log.info("PUT /api/v1/fees/{} - Updating fee", id);
        // đưa id từ path vào payload để đảm bảo khớp
        req.setId(id);
        return ResponseEntity.ok(feeCrudService.update(req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete fee", description = "Delete a fee")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<String>> deleteFee(@PathVariable Long id) {
        log.info("DELETE /api/v1/fees/{} - Deleting fee", id);
        return ResponseEntity.ok(feeCrudService.delete(id));
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate fee", description = "Deactivate a fee")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<String>> deactivateFee(@PathVariable Long id) {
        log.info("PUT /api/v1/fees/{}/deactivate - Deactivating fee", id);
        feeCrudService.deactivate(id);

        ApiResponse<String> res = new ApiResponse<>();
        res.setCode(HttpStatus.OK.value());
        res.setMessage("Fee deactivated successfully");
        return ResponseEntity.ok(res);
    }

    @PostMapping("/monthly/generate")
    @Operation(summary = "Generate monthly fees", description = "Generate monthly fees for all apartments")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<List<Fee>> generateMonthlyFees(
            @RequestParam FeeTypeEnum feeType,
            @RequestParam String billingMonth,
            @RequestParam(required = false) BigDecimal unitPricePerSqm,
            @RequestParam(required = false) String customFeeName) {

        log.info("POST /api/v1/fees/monthly/generate - Generating monthly fees for {}", billingMonth);
        YearMonth ym = YearMonth.parse(billingMonth);     // đảm bảo format hợp lệ
        List<Fee> fees = monthlyGenService.generateForAll(
                feeType, ym, unitPricePerSqm, customFeeName);
        return ResponseEntity.ok(fees);
    }

    @GetMapping("/monthly/unpaid/apartment/{apartmentId}")
    @Operation(summary = "Get unpaid monthly fees by apartment", description = "Retrieve unpaid monthly fees for a specific apartment")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'MANAGER')")
    public ResponseEntity<List<Fee>> getUnpaidMonthlyFees(@PathVariable Long apartmentId) {
        log.info("GET /api/v1/fees/monthly/unpaid/apartment/{} - Fetching unpaid monthly fees", apartmentId);
        return ResponseEntity.ok(monthlyGenService
                .getUnpaidMonthlyFeesByApartment(apartmentId));
    }

    @GetMapping("/monthly/month/{billingMonth}")
    @Operation(summary = "Get monthly fees by month", description = "Retrieve monthly fees for a specific month")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'MANAGER')")
    public ResponseEntity<List<Fee>> getMonthlyFeesByMonth(@PathVariable String billingMonth) {
        log.info("GET /api/v1/fees/monthly/month/{} - Fetching monthly fees by month", billingMonth);
        return ResponseEntity.ok(monthlyGenService
                .getMonthlyFeesByMonth(billingMonth));
    }

    @GetMapping("/apartment/{apartmentId}")
    @Operation(summary = "Get fees by apartment", description = "Retrieve all fees for a specific apartment")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'MANAGER')")
    public ResponseEntity<List<FeeResponse>> getFeesByApartment(@PathVariable Long apartmentId) {
        log.info("GET /api/v1/fees/apartment/{} - Fetching fees by apartment", apartmentId);
        List<FeeResponse> fees = feeCrudService.getFeesByApartment(apartmentId);
        return ResponseEntity.ok(fees);
    }

    @GetMapping("/apartment/{apartmentId}/unpaid")
    @Operation(summary = "Get unpaid fees by apartment", description = "Retrieve unpaid fees for a specific apartment")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'MANAGER')")
    public ResponseEntity<List<FeeResponse>> getUnpaidFeesByApartment(@PathVariable Long apartmentId) {
        log.info("GET /api/v1/fees/apartment/{}/unpaid - Fetching unpaid fees by apartment", apartmentId);
        List<FeeResponse> fees = feeCrudService.getUnpaidFeesByApartment(apartmentId);
        return ResponseEntity.ok(fees);
    }

    @GetMapping("/apartment/{apartmentId}/summary")
    @Operation(summary = "Get apartment fee summary", description = "Retrieve fee summary for a specific apartment")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'MANAGER')")
    public ResponseEntity<ApiResponse<Object>> getApartmentFeeSummary(@PathVariable Long apartmentId) {
        log.info("GET /api/v1/fees/apartment/{}/summary - Fetching apartment fee summary", apartmentId);
        List<FeeResponse> allFees = feeCrudService.getFeesByApartment(apartmentId);
        List<FeeResponse> unpaidFees = feeCrudService.getUnpaidFeesByApartment(apartmentId);
        
        class FeeSummary {
            public final int totalFees = allFees.size();
            public final int unpaidFeesCount = unpaidFees.size();
            public final int paidFees = totalFees - unpaidFeesCount;
            public final List<FeeResponse> unpaidFeesList = unpaidFees;
        }
        
        FeeSummary summary = new FeeSummary();

        ApiResponse<Object> response = new ApiResponse<>();
        response.setCode(HttpStatus.OK.value());
        response.setMessage("Apartment fee summary retrieved successfully");
        response.setData(summary);
        
        return ResponseEntity.ok(response);
    }
}
