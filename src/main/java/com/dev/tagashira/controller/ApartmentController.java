package com.dev.tagashira.controller;

import com.dev.tagashira.dto.request.ApartmentCreateRequest;
import com.dev.tagashira.dto.request.ApartmentUpdateRequest;
import com.dev.tagashira.dto.response.ApartmentResponse;
import com.dev.tagashira.dto.response.ApiResponse;
import com.dev.tagashira.dto.response.PaginatedResponse;
import com.dev.tagashira.entity.Apartment;
import com.dev.tagashira.service.ApartmentService;
import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/apartments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Apartment Management", description = "APIs for managing apartments")
@Slf4j
public class ApartmentController {
    ApartmentService apartmentService;    
    
    @PostMapping
    @Operation(summary = "Create apartment", description = "Create a new apartment")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApartmentResponse> createOne(@Valid @RequestBody ApartmentCreateRequest request) {
        log.info("POST /api/v1/apartments - Creating new apartment");
        ApartmentResponse apartment = apartmentService.create(request);
        return ResponseEntity.status(HttpStatus.OK).body(apartment);
    }    
    
    @GetMapping
    @Operation(summary = "Get all apartments", description = "Retrieve all apartments with pagination and filtering")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<PaginatedResponse<ApartmentResponse>> getAll(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Filter Specification<Apartment> spec) {

        log.info("GET /api/v1/apartments - Fetching apartments with pagination");
        Pageable pageable = PageRequest.of(page - 1, size);
        PaginatedResponse<ApartmentResponse> result = apartmentService.getAll(spec, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }    
    
    @GetMapping("/{id}")
    @Operation(summary = "Get apartment by ID", description = "Retrieve apartment details by ID")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApartmentResponse> getDetail(@PathVariable Long id){
        log.info("GET /api/v1/apartments/{} - Fetching apartment details", id);
        ApartmentResponse apartment = apartmentService.getDetail(id);
        return ResponseEntity.status(HttpStatus.OK).body(apartment);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update apartment", description = "Update apartment information")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApartmentResponse> updateOne(@PathVariable Long id, @RequestBody ApartmentUpdateRequest request){
        log.info("PUT /api/v1/apartments/{} - Updating apartment", id);
        ApartmentResponse apartment =  apartmentService.update(id,request);
        return ResponseEntity.status(HttpStatus.OK).body(apartment);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete apartment", description = "Delete an apartment")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<String>> deleteApartment(@PathVariable Long id) throws Exception {
        log.info("DELETE /api/v1/apartments/{} - Deleting apartment", id);
        ApiResponse<String> response = apartmentService.deleteApartment(id);
        return ResponseEntity.ok(response);
    }
}
