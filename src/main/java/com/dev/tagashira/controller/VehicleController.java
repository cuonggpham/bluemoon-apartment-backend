package com.dev.tagashira.controller;


import com.dev.tagashira.dto.response.ApiResponse;
import com.dev.tagashira.dto.response.PaginatedResponse;
import com.dev.tagashira.dto.response.VehicleResponse;
import com.dev.tagashira.dto.response.VehicleCountSummary;
import com.dev.tagashira.entity.Vehicle;
import com.dev.tagashira.constant.VehicleEnum;
import com.dev.tagashira.service.VehicleService;
import com.turkraft.springfilter.boot.Filter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@CrossOrigin(origins = "http://localhost:5173")
public class VehicleController {
    VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<VehicleResponse> addVehicle(@RequestBody Vehicle vehicle) {
        return ResponseEntity.ok(this.vehicleService.create(vehicle));
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<VehicleResponse>> getAllVehicles(@Filter Specification<Vehicle> spec,
                                                                     @RequestParam(value = "page", defaultValue = "1") int page,
                                                                     @RequestParam(value = "size", defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page - 1, size);
        PaginatedResponse<VehicleResponse> result = vehicleService.getAll(spec, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<VehicleResponse>> getAllVehiclesById(@PathVariable("id") long apartmentId) {
        return ResponseEntity.ok(this.vehicleService.findAllByApartmentId(apartmentId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteVehicle(@PathVariable("id") Long apartmentId, @RequestBody Vehicle vehicle) {
        return this.vehicleService.deleteVehicle(apartmentId, vehicle);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> updateVehicle(@PathVariable("id") String id, @RequestBody Vehicle vehicle) {
        return ResponseEntity.ok(this.vehicleService.update(id, vehicle));
    }

    // ============ VEHICLE COUNT APIs FOR FEE CALCULATION ============
    
    /**
     * Get vehicle count summary for an apartment (for fee calculation)
     */
    @GetMapping("/count/apartment/{apartmentId}")
    public ResponseEntity<ApiResponse<VehicleCountSummary>> getVehicleCountSummary(@PathVariable("apartmentId") Long apartmentId) {
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
    public ResponseEntity<ApiResponse<Long>> getVehicleCountByType(
            @PathVariable("apartmentId") Long apartmentId, 
            @PathVariable("vehicleType") VehicleEnum vehicleType) {
        Long count = vehicleService.getVehicleCountByType(apartmentId, vehicleType);
        
        ApiResponse<Long> response = new ApiResponse<>();
        response.setCode(HttpStatus.OK.value());
        response.setMessage("Vehicle count retrieved successfully");
        response.setData(count);
        
        return ResponseEntity.ok(response);
    }
}
