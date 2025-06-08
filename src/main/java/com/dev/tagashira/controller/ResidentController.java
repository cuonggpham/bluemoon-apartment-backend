package com.dev.tagashira.controller;

import com.dev.tagashira.dto.request.ResidentCreateRequest;
import com.dev.tagashira.dto.request.ResidentUpdateRequest;
import com.dev.tagashira.dto.request.ResidentWithApartmentCreateRequest;
import com.dev.tagashira.dto.response.ApiResponse;
import com.dev.tagashira.dto.response.PaginatedResponse;
import com.dev.tagashira.dto.response.ResidentResponse;
import com.dev.tagashira.entity.Apartment;
import com.dev.tagashira.entity.Resident;
import com.dev.tagashira.service.ResidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/residents")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Resident Management", description = "APIs for managing residents")
@Slf4j
public class ResidentController {
    private final ResidentService residentService;

    @GetMapping()
    @Operation(summary = "Get all residents", description = "Retrieve all residents with pagination and filtering")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<PaginatedResponse<ResidentResponse>> getAllResidents(@Filter Specification<Resident> spec,
                                                                       @RequestParam(value = "page", defaultValue = "1") int page,
                                                                       @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("GET /api/v1/residents - Fetching residents with pagination");
        Pageable pageable = PageRequest.of(page - 1, size);
        PaginatedResponse<ResidentResponse> residentResponses = this.residentService.fetchAllResidents(spec, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(residentResponses);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all residents without pagination", description = "Retrieve all residents without pagination")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<PaginatedResponse<ResidentResponse>> getAll(@Filter Specification<Resident> spec,
                                                                       @RequestParam(value = "page", defaultValue = "1") int page,
                                                                       @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("GET /api/v1/residents/all - Fetching all residents");
        Pageable pageable = PageRequest.of(page - 1, size);
        PaginatedResponse<ResidentResponse> residentResponses = this.residentService.fetchAll(spec, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(residentResponses);
    }    
    
    @GetMapping("/{id}")
    @Operation(summary = "Get resident by ID", description = "Retrieve resident details by ID")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ResidentResponse> getResidentById(@PathVariable("id") long id) throws Exception {
        log.info("GET /api/v1/residents/{} - Fetching resident by ID", id);
        ResidentResponse fetchResident = this.residentService.fetchResidentById(id);
        return ResponseEntity.status(HttpStatus.OK).body(fetchResident);
    }

    @PostMapping("/with-apartment")
    @Operation(summary = "Create resident with apartment", description = "Create a new resident with apartment assignment")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ResidentResponse> createResidentWithApartment(@Valid @RequestBody ResidentWithApartmentCreateRequest request) throws Exception {
        log.info("POST /api/v1/residents/with-apartment - Creating resident with apartment");
        try {
            ResidentResponse resident = this.residentService.createResidentWithApartment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(resident);
        } catch (Exception e) {
            log.error("Error creating resident with apartment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping()
    @Operation(summary = "Create new resident", description = "Create a new resident")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ResidentResponse> createNewUser(@Valid @RequestBody ResidentCreateRequest apiResident) throws Exception {
        log.info("POST /api/v1/residents - Creating new resident");
        try {
            ResidentResponse resident = this.residentService.createResident(apiResident);
            if (resident == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(resident);
        } catch (Exception e) {
            log.error("Error creating resident: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete resident", description = "Delete a resident by ID")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<String>> deleteResident(@PathVariable("id") long id) throws Exception {
        log.info("DELETE /api/v1/residents/{} - Deleting resident", id);
        ApiResponse<String> response = this.residentService.deleteResident(id);
        return ResponseEntity.ok(response);
    }    
    
    @DeleteMapping("/{id}/force")
    @Operation(summary = "Force delete resident", description = "Force delete resident with owner cleanup")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<String>> forceDeleteResident(
            @PathVariable("id") long id,
            @RequestParam(value = "forceDelete", defaultValue = "true") boolean forceDelete) throws Exception {
        log.info("DELETE /api/v1/residents/{}/force - Force deleting resident", id);
        ApiResponse<String> response = this.residentService.deleteResidentWithOwnerCleanup(id, forceDelete);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/is-owner")
    @Operation(summary = "Check if resident is owner", description = "Check if a resident is an apartment owner")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<Boolean> isResidentOwner(@PathVariable("id") long id) {
        log.info("GET /api/v1/residents/{}/is-owner - Checking if resident is owner", id);
        boolean isOwner = this.residentService.isResidentOwner(id);
        return ResponseEntity.ok(isOwner);
    }
    
    @GetMapping("/{id}/owned-apartments")
    @Operation(summary = "Get owned apartments", description = "Get apartments owned by a resident")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<List<Apartment>> getOwnedApartments(@PathVariable("id") long id) {
        log.info("GET /api/v1/residents/{}/owned-apartments - Fetching owned apartments", id);
        List<Apartment> ownedApartments = this.residentService.getApartmentsOwnedByResident(id);
        return ResponseEntity.ok(ownedApartments);
    }
      
    @PutMapping()
    @Operation(summary = "Update resident", description = "Update resident information")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ResidentResponse> updateUser(@RequestBody ResidentUpdateRequest apiResident) throws Exception {
        log.info("PUT /api/v1/residents - Updating resident");
        ResidentResponse resident = this.residentService.updateResident(apiResident);
        return ResponseEntity.ok(resident);
    }    
    
    @PutMapping("/{id}")
    @Operation(summary = "Update resident by ID", description = "Update resident information by ID")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ResidentResponse> updateUserById(@PathVariable("id") long id, @RequestBody ResidentCreateRequest apiResident) throws Exception {
        log.info("PUT /api/v1/residents/{} - Updating resident by ID", id);
        try {
            ResidentResponse updatedResident = this.residentService.updateResidentById(id, apiResident);
            return ResponseEntity.ok(updatedResident);
        } catch (Exception e) {
            log.error("Error updating resident: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
