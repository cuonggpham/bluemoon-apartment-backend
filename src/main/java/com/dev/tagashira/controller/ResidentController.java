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
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/residents")
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
public class ResidentController {
    private final ResidentService residentService;    //fetch all residentList

    @GetMapping()
    public ResponseEntity<PaginatedResponse<ResidentResponse>> getAllResidents(@Filter Specification<Resident> spec,
                                                                       @RequestParam(value = "page", defaultValue = "1") int page,
                                                                       @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        PaginatedResponse<ResidentResponse> residentResponses = this.residentService.fetchAllResidents(spec, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(residentResponses);
    }

    @GetMapping("/all")
    public ResponseEntity<PaginatedResponse<ResidentResponse>> getAll(@Filter Specification<Resident> spec,
                                                                       @RequestParam(value = "page", defaultValue = "1") int page,
                                                                       @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        PaginatedResponse<ResidentResponse> residentResponses = this.residentService.fetchAll(spec, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(residentResponses);
    }    
    
    //fetch resident by id
    @GetMapping("/{id}")
    public ResponseEntity<ResidentResponse> getResidentById(@PathVariable("id") long id) throws Exception {
        ResidentResponse fetchResident = this.residentService.fetchResidentById(id);
        return ResponseEntity.status(HttpStatus.OK).body(fetchResident);
    }

    //Create new resident with apartment (transactional)
    @PostMapping("/with-apartment")
    public ResponseEntity<ResidentResponse> createResidentWithApartment(@Valid @RequestBody ResidentWithApartmentCreateRequest request) throws Exception {
        try {
            ResidentResponse resident = this.residentService.createResidentWithApartment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(resident);
        } catch (Exception e) {
            log.error("Error creating resident with apartment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    //Create new resident
    @PostMapping()
    public ResponseEntity<ResidentResponse> createNewUser(@Valid @RequestBody ResidentCreateRequest apiResident) throws Exception {
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

    //Delete resident by id
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteResident(@PathVariable("id") long id) throws Exception {
        ApiResponse<String> response = this.residentService.deleteResident(id);
        return ResponseEntity.ok(response);
    }    
    
    //Force delete resident by id with owner cleanup
    @DeleteMapping("/{id}/force")
    public ResponseEntity<ApiResponse<String>> forceDeleteResident(
            @PathVariable("id") long id,
            @RequestParam(value = "forceDelete", defaultValue = "true") boolean forceDelete) throws Exception {
        ApiResponse<String> response = this.residentService.deleteResidentWithOwnerCleanup(id, forceDelete);
        return ResponseEntity.ok(response);
    }
    
    //Check if resident is an owner
    @GetMapping("/{id}/is-owner")
    public ResponseEntity<Boolean> isResidentOwner(@PathVariable("id") long id) {
        boolean isOwner = this.residentService.isResidentOwner(id);
        return ResponseEntity.ok(isOwner);
    }
    
    //Get apartments owned by resident
    @GetMapping("/{id}/owned-apartments")
    public ResponseEntity<List<Apartment>> getOwnedApartments(@PathVariable("id") long id) {
        List<Apartment> ownedApartments = this.residentService.getApartmentsOwnedByResident(id);
        return ResponseEntity.ok(ownedApartments);
    }
      
    //Update resident
    @PutMapping()
    public ResponseEntity<ResidentResponse> updateUser(@RequestBody ResidentUpdateRequest apiResident) throws Exception {
        ResidentResponse resident = this.residentService.updateResident(apiResident);
        return ResponseEntity.ok(resident);
    }    
    
    //Update resident by ID
    @PutMapping("/{id}")
    public ResponseEntity<ResidentResponse> updateUserById(@PathVariable("id") long id, @RequestBody ResidentCreateRequest apiResident) throws Exception {
        try {
            ResidentResponse updatedResident = this.residentService.updateResidentById(id, apiResident);
            return ResponseEntity.ok(updatedResident);
        } catch (Exception e) {
            log.error("Error updating resident: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
