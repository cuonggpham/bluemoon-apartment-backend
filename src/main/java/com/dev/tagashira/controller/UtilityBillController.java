package com.dev.tagashira.controller;

import com.dev.tagashira.dto.response.PaginatedResponse;
import com.dev.tagashira.entity.Resident;
import com.dev.tagashira.entity.UtilityBill;
import com.dev.tagashira.service.UtilityBillService;
import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/v1/utilitybills")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Utility Bill Management", description = "APIs for managing utility bills")
@Slf4j
public class UtilityBillController {
    UtilityBillService utilityBillService;
    
    @PostMapping("/import")
    @Operation(summary = "Import utility bills", description = "Import utility bills from Excel file")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<?> importUtilityBills(@RequestParam("file") MultipartFile file, @RequestParam("name") String name) {
        log.info("POST /api/v1/utilitybills/import - Importing utility bills from file: {}", name);
        List<UtilityBill> utilityBills = utilityBillService.importExcel(file,name);
        return ResponseEntity.ok(utilityBills);
    }

    @GetMapping
    @Operation(summary = "Get all utility bills", description = "Retrieve all utility bills with pagination and filtering")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'MANAGER')")
    public ResponseEntity<?> getAllUtilityBills(@Filter Specification<UtilityBill> spec,
                                                @RequestParam(value = "page", defaultValue = "1") int page,
                                                @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("GET /api/v1/utilitybills - Fetching utility bills with pagination");
        Pageable pageable = PageRequest.of(page - 1, size);
        PaginatedResponse<UtilityBill> responses = utilityBillService.fetchUtilityBills(spec, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get utility bills by apartment", description = "Get utility bills for a specific apartment")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'MANAGER')")
    public ResponseEntity<List<UtilityBill>> getUtilityBillByApartmentId(@PathVariable("id") Long id) {
        log.info("GET /api/v1/utilitybills/{} - Fetching utility bills by apartment", id);
        return ResponseEntity.status(HttpStatus.OK).body(utilityBillService.fetchUtilityBillsByApartmentId(id));
    }

    @PostMapping("/update/{id}")
    @Operation(summary = "Update utility bill", description = "Update utility bill information")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<UtilityBill> updateUtilityBill(@PathVariable("id") Long id) {
        log.info("POST /api/v1/utilitybills/update/{} - Updating utility bill", id);
        return ResponseEntity.status(HttpStatus.OK).body(utilityBillService.updateUtilityBill(id));
    }
}
