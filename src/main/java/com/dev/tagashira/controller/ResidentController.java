package com.dev.tagashira.controller;

import com.dev.tagashira.dto.request.ResidentCreateRequest;
import com.dev.tagashira.dto.request.ResidentUpdateRequest;
import com.dev.tagashira.dto.request.ResidentWithApartmentCreateRequest;
import com.dev.tagashira.dto.response.ApiResponse;
import com.dev.tagashira.dto.response.PaginatedResponse;
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

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/residents")
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
public class ResidentController {
    private final ResidentService residentService;

    //fetch all residents
    @GetMapping()
    public ResponseEntity<PaginatedResponse<Resident>> getAllResidents(@Filter Specification<Resident> spec,
                                                                       @RequestParam(value = "page", defaultValue = "1") int page,
                                                                       @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        PaginatedResponse<Resident> residentResponses = this.residentService.fetchAllResidents(spec, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(residentResponses);
    }

    @GetMapping("/all")
    public ResponseEntity<PaginatedResponse<Resident>> getAll(@Filter Specification<Resident> spec,
                                                                       @RequestParam(value = "page", defaultValue = "1") int page,
                                                                       @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        PaginatedResponse<Resident> residentResponses = this.residentService.fetchAll(spec, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(residentResponses);
    }

    //fetch resident by id
    @GetMapping("/{id}")
    public ResponseEntity<Resident> getResidentById(@PathVariable("id") long id) throws Exception {
        Resident fetchResident = this.residentService.fetchResidentById(id);
        return ResponseEntity.status(HttpStatus.OK).body(fetchResident);
    }    //Create new resident with apartment (transactional)
    @PostMapping("/with-apartment")
    public ResponseEntity<Resident> createResidentWithApartment(@Valid @RequestBody ResidentWithApartmentCreateRequest request) throws Exception {
        try {
            Resident resident = this.residentService.createResidentWithApartment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(resident);
        } catch (Exception e) {
            log.error("Error creating resident with apartment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    //Create new resident
    @PostMapping()
    public ResponseEntity<Resident> createNewUser(@Valid @RequestBody ResidentCreateRequest apiResident) throws Exception {
        try {
            Resident resident = this.residentService.createResident(apiResident);
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
    
    //Update resident
    @PutMapping()
    public ResponseEntity<Resident> updateUser(@RequestBody ResidentUpdateRequest apiResident) throws Exception {
        Resident resident = this.residentService.updateResident(apiResident);
        return ResponseEntity.ok(resident);
    }    
    
    //Update resident by ID
    @PutMapping("/{id}")
    public ResponseEntity<Resident> updateUserById(@PathVariable("id") long id, @RequestBody ResidentCreateRequest apiResident) throws Exception {
        try {
            Resident updatedResident = this.residentService.updateResidentById(id, apiResident);
            return ResponseEntity.ok(updatedResident);
        } catch (Exception e) {
            log.error("Error updating resident: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
