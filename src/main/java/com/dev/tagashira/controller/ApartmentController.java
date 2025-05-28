package com.dev.tagashira.controller;

import com.dev.tagashira.dto.request.ApartmentCreateRequest;
import com.dev.tagashira.dto.request.ApartmentUpdateRequest;
import com.dev.tagashira.dto.response.ApartmentResponse;
import com.dev.tagashira.dto.response.ApiResponse;
import com.dev.tagashira.dto.response.PaginatedResponse;
import com.dev.tagashira.entity.Apartment;
import com.dev.tagashira.service.ApartmentService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/apartments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@CrossOrigin(origins = "http://localhost:5173")
public class ApartmentController {
    ApartmentService apartmentService;    
    
    @PostMapping
    public ResponseEntity<ApartmentResponse> createOne(@Valid @RequestBody ApartmentCreateRequest request) {
        ApartmentResponse apartment = apartmentService.create(request);
        return ResponseEntity.status(HttpStatus.OK).body(apartment);
    }    
    
    @GetMapping
    public ResponseEntity<PaginatedResponse<ApartmentResponse>> getAll(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Filter Specification<Apartment> spec) {

        Pageable pageable = PageRequest.of(page - 1, size);
        PaginatedResponse<ApartmentResponse> result = apartmentService.getAll(spec, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }    
    
    @GetMapping("/{id}")
    public ResponseEntity<ApartmentResponse> getDetail(@PathVariable Long id){
        ApartmentResponse apartment = apartmentService.getDetail(id);
        return ResponseEntity.status(HttpStatus.OK).body(apartment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApartmentResponse> updateOne(@PathVariable Long id, @RequestBody ApartmentUpdateRequest request){
        ApartmentResponse apartment =  apartmentService.update(id,request);
        return ResponseEntity.status(HttpStatus.OK).body(apartment);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteApartment(@PathVariable Long id) throws Exception {
        ApiResponse<String> response = apartmentService.deleteApartment(id);
        return ResponseEntity.ok(response);
    }
}
