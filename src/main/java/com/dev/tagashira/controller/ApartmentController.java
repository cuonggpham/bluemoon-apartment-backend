package com.dev.tagashira.controller;

import com.dev.tagashira.dto.request.ApartmentCreateRequest;
 import com.dev.tagashira.dto.request.ApartmentUpdateRequest;
 import com.dev.tagashira.dto.response.PaginatedResponse;
 import com.dev.tagashira.entity.Apartment;
 import com.dev.tagashira.service.ApartmentService;
 import com.turkraft.springfilter.boot.Filter;
 import jakarta.validation.Valid;
 import lombok.AccessLevel;
 import lombok.AllArgsConstructor;
 import lombok.RequiredArgsConstructor;
 import lombok.experimental.FieldDefaults;
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
     public ResponseEntity<Apartment> createOne(@Valid @RequestBody ApartmentCreateRequest request) {
         Apartment apartment = apartmentService.create(request);
         return ResponseEntity.status(HttpStatus.OK).body(apartment);
     }
 
     @GetMapping()
     public ResponseEntity<PaginatedResponse<Apartment>> getAll(@Filter Specification<Apartment> spec, Pageable pageable){
         PaginatedResponse<Apartment> result = apartmentService.getAll(spec, pageable);
         return ResponseEntity.status(HttpStatus.OK).body(result);
     }
 
     @GetMapping("/{id}")
     public ResponseEntity<Apartment> getDetail(@PathVariable Long id){
         Apartment apartment = apartmentService.getDetail(id);
         return ResponseEntity.status(HttpStatus.OK).body(apartment);
     }
 
     @PutMapping("/{id}")
     public ResponseEntity<Apartment> updateOne(@PathVariable Long id, @RequestBody ApartmentUpdateRequest request){
         Apartment apartment =  apartmentService.update(id,request);
         return ResponseEntity.status(HttpStatus.OK).body(apartment);
     }
 }
