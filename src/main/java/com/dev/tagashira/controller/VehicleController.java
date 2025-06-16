package com.dev.tagashira.controller;


import com.dev.tagashira.dto.response.ApiResponse;
import com.dev.tagashira.dto.response.PaginatedResponse;
import com.dev.tagashira.dto.response.VehicleResponse;
import com.dev.tagashira.dto.response.VehicleCountSummary;
import com.dev.tagashira.entity.Vehicle;
import com.dev.tagashira.constant.VehicleEnum;
import com.dev.tagashira.service.VehicleService;
import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Vehicle Management", description = "APIs for managing vehicles")
@Slf4j
public class VehicleController {
    VehicleService vehicleService;

    @PostMapping
    @Operation(summary = "Add vehicle", description = "Add a new vehicle")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<VehicleResponse> addVehicle(@RequestBody Vehicle vehicle) {
        log.info("POST /api/v1/vehicles - Adding new vehicle");
        return ResponseEntity.ok(this.vehicleService.create(vehicle));
    }

    @GetMapping
    @Operation(summary = "Get all vehicles", description = "Retrieve all vehicles with pagination and filtering")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<PaginatedResponse<VehicleResponse>> getAllVehicles(@Filter Specification<Vehicle> spec,
                                                                     @RequestParam(value = "page", defaultValue = "1") int page,
                                                                     @RequestParam(value = "size", defaultValue = "10") int size){
        log.info("GET /api/v1/vehicles - Fetching vehicles with pagination");
        Pageable pageable = PageRequest.of(page - 1, size);
        PaginatedResponse<VehicleResponse> result = vehicleService.getAll(spec, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicles by apartment", description = "Get all vehicles for a specific apartment")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<VehicleResponse>> getAllVehiclesById(@PathVariable("id") long apartmentId) {
        log.info("GET /api/v1/vehicles/{} - Fetching vehicles by apartment", apartmentId);
        return ResponseEntity.ok(this.vehicleService.findAllByApartmentId(apartmentId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete vehicle", description = "Delete a vehicle")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<String> deleteVehicle(@PathVariable("id") Long apartmentId, @RequestBody Vehicle vehicle) {
        log.info("DELETE /api/v1/vehicles/{} - Deleting vehicle", apartmentId);
        return this.vehicleService.deleteVehicle(apartmentId, vehicle);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle", description = "Update vehicle information")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<VehicleResponse> updateVehicle(@PathVariable("id") String id, @RequestBody Vehicle vehicle) {
        log.info("PUT /api/v1/vehicles/{} - Updating vehicle", id);
        return ResponseEntity.ok(this.vehicleService.update(id, vehicle));
    }

    // ============ VEHICLE COUNT APIs FOR FEE CALCULATION ============
    
    /**
     * Get vehicle count summary for an apartment (for fee calculation)
     */
    @GetMapping("/count/apartment/{apartmentId}")
    @Operation(summary = "Get vehicle count summary", description = "Get vehicle count summary for fee calculation")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'MANAGER')")
    public ResponseEntity<ApiResponse<VehicleCountSummary>> getVehicleCountSummary(@PathVariable("apartmentId") Long apartmentId) {
        log.info("GET /api/v1/vehicles/count/apartment/{} - Fetching vehicle count summary", apartmentId);
        VehicleCountSummary summary = vehicleService.getVehicleCountSummary(apartmentId);
        
        ApiResponse<VehicleCountSummary> response = new ApiResponse<>();
        response.setCode(HttpStatus.OK.value());
        response.setMessage("Vehicle count summary retrieved successfully");
        response.setData(summary);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get vehicle count for specific type and apartment
     */
    @GetMapping("/count/apartment/{apartmentId}/type/{vehicleType}")
    @Operation(summary = "Get vehicle count by type", description = "Get vehicle count for specific type and apartment")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'MANAGER')")
    public ResponseEntity<ApiResponse<Long>> getVehicleCountByType(
            @PathVariable("apartmentId") Long apartmentId, 
            @PathVariable("vehicleType") VehicleEnum vehicleType) {
        log.info("GET /api/v1/vehicles/count/apartment/{}/type/{} - Fetching vehicle count by type", apartmentId, vehicleType);
        Long count = vehicleService.getVehicleCountByType(apartmentId, vehicleType);
        
        ApiResponse<Long> response = new ApiResponse<>();
        response.setCode(HttpStatus.OK.value());
        response.setMessage("Vehicle count retrieved successfully");
        response.setData(count);
        
        return ResponseEntity.ok(response);
    }
}
